package com.doogoo.doogoo.calendar.application;

import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.dodream.api.dto.IssueDoDreamIcsRequest;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
import com.doogoo.doogoo.subscription.application.SubscriptionReader;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;


@Service
public class IcsService {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter ICS_SEOUL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(SEOUL);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final Pattern SPECIAL = Pattern.compile("([\\\\,;])");
    private static final int DEFAULT_ALARM_MINUTES = 60;
    private static final String KEY_ACADEMIC = "ACADEMIC";
    private static final String KEY_DODREAM = "DODREAM";

    private final AcademicScheduleRepository academicScheduleRepository;
    private final EventRepository eventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionReader subscriptionReader;
    private final ObjectMapper objectMapper;
    private final Cache<String, String> icsCache;
    private final Cache<String, Subscription> tokenCache;
    private final Cache<String, List<AcademicSchedule>> academicNoticesCache;
    private final Cache<String, List<Event>> doDreamNoticesCache;
    private final Semaphore semaphore;

    public IcsService(AcademicScheduleRepository academicScheduleRepository,
                      EventRepository eventRepository,
                      SubscriptionRepository subscriptionRepository,
                      SubscriptionReader subscriptionReader,
                      ObjectMapper objectMapper,
                      Cache<String, String> icsCache,
                      Cache<String, Subscription> tokenCache,
                      Cache<String, List<AcademicSchedule>> academicNoticesCache,
                      Cache<String, List<Event>> doDreamNoticesCache,
                      Semaphore semaphore) {
        this.academicScheduleRepository = academicScheduleRepository;
        this.eventRepository = eventRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionReader = subscriptionReader;
        this.objectMapper = objectMapper;
        this.icsCache = icsCache;
        this.tokenCache = tokenCache;
        this.academicNoticesCache = academicNoticesCache;
        this.doDreamNoticesCache = doDreamNoticesCache;
        this.semaphore = semaphore;
    }

    public Subscription getSubscriptionByToken(String token) {
        long start = System.currentTimeMillis();
        boolean[] isMiss = {false};
        Subscription sub = tokenCache.get(token, k -> {
                    isMiss[0] = true;
                    Subscription s = subscriptionReader.getByToken(k);
                    touch(k);
                    return s;
                }
        );
        long latency = System.currentTimeMillis() - start;
        JsonLog.debug(IcsService.class, new LogDto.IcsCache(
                "token.cache.complete",
                token,
                sub.getFilterHash(),
                !isMiss[0],
                latency
        ));

        return sub;
    }

    public String getIcsByToken(String token) {
        Subscription sub = getSubscriptionByToken(token);
        String key = sub.getSourceType().name() + ":" + sub.getFilterHash();

        long start = System.currentTimeMillis();
        boolean[] isMiss = {false};

        String ics = icsCache.get(key, k -> {
            isMiss[0] = true;
            return renderWithLimit(sub);
        });

        long latency = System.currentTimeMillis() - start;

        JsonLog.debug(IcsService.class, new LogDto.IcsCache(
                "ics.cache.complete",
                token,
                sub.getFilterHash(),
                !isMiss[0],
                latency
        ));

        return ics;
    }

    public void invalidateAcademicDataByUpdate() {
        int noticeCacheSize = academicNoticesCache.asMap().size();

        academicNoticesCache.invalidateAll();

        int removed = invalidateIcsCacheBySourceType(KEY_ACADEMIC);

        JsonLog.info(IcsService.class, new LogDto.CacheInvalidateLog(
                "academic.cache.invalidate",
                KEY_ACADEMIC,
                noticeCacheSize,
                removed
        ));
    }

    public void invalidateDoDreamDataByUpdate() {
        int noticeCacheSize = doDreamNoticesCache.asMap().size();
        doDreamNoticesCache.invalidateAll();

        int removed = invalidateIcsCacheBySourceType(KEY_DODREAM);

        JsonLog.info(IcsService.class, new LogDto.CacheInvalidateLog(
                "dodream.cache.invalidate",
                KEY_DODREAM,
                noticeCacheSize,
                removed
        ));
    }

    private int invalidateIcsCacheBySourceType(String type) {
        int before = icsCache.asMap().size();
        icsCache.asMap().keySet().removeIf(key -> key.startsWith(type + ":"));
        int after = icsCache.asMap().size();
        return before - after;
    }

    private String renderWithLimit(Subscription subscription) {
        boolean acquired = false;
        long start = System.currentTimeMillis();

        JsonLog.info(IcsService.class, new LogDto.IcsRenderLog(
                "ics.render.start",
                subscription.getToken(),
                subscription.getSourceType().name(),
                subscription.getFilterHash(),
                null,
                null
        ));

        try {
            semaphore.acquire();
            acquired = true;
            RenderResult rendered = render(subscription);

            long latency = System.currentTimeMillis() - start;
            JsonLog.info(IcsService.class, new LogDto.IcsRenderLog(
                    "ics.render.complete",
                    subscription.getToken(),
                    subscription.getSourceType().name(),
                    subscription.getFilterHash(),
                    rendered.eventCount(),
                    latency
            ));

            return rendered.ics();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DoogooException(ErrorCode.INTERNAL_SERVER_ERROR, e);

        } finally {
            if (acquired) semaphore.release();
        }
    }


    private void touch(String token) {
        subscriptionRepository.updateLastAccessedAtByToken(token, Instant.now());
    }


    private RenderResult render(Subscription subscription) {
        String now = ICS_UTC.format(Instant.now());
        StringBuilder sb = new StringBuilder();

        int eventCount = 0;

        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Doogoo//Calendar 1.0//KO\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        if (subscription.getSourceType() == SourceType.ACADEMIC) {
            List<AcademicSchedule> all = getAcademicSchedules();
            IssueAcademicIcsRequest filter = parsePayload(
                    subscription.getPayload(),
                    IssueAcademicIcsRequest.class,
                    subscription.getToken(),
                    subscription.getSourceType().name()
            );
            List<AcademicSchedule> filtered = all.stream().filter(n -> passesAcademicFilter(n, filter)).toList();
            if (filtered.isEmpty()) {
                filtered = all;
            }
            eventCount = filtered.size();

            for (AcademicSchedule n : filtered) {
                appendAcademicEvent(sb, subscription, n, now);
            }
        } else if (subscription.getSourceType() == SourceType.DODREAM) {
            List<Event> all = getDoDreamEvents();
            IssueDoDreamIcsRequest filter = parsePayload(
                    subscription.getPayload(),
                    IssueDoDreamIcsRequest.class,
                    subscription.getToken(),
                    subscription.getSourceType().name()
            );
            List<Event> filtered = all.stream().filter(n -> passesDoDreamFilter(n, filter)).toList();
            if (filtered.isEmpty()) {
                filtered = all;
            }
            eventCount = filtered.size();

            for (Event n : filtered) {
                appendDoDreamEvent(sb, subscription, n, now);
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return new RenderResult(fold(sb.toString()), eventCount);
    }

    private record RenderResult(
            String ics,
            int eventCount
    ) {
    }

    private List<AcademicSchedule> getAcademicSchedules() {
        long start = System.currentTimeMillis();
        boolean[] isMiss = {false};

        List<AcademicSchedule> schedules = academicNoticesCache.get(KEY_ACADEMIC, k -> {
                    isMiss[0] = true;
                    return academicScheduleRepository.findAll();
                }
        );

        long latency = System.currentTimeMillis() - start;

        JsonLog.debug(IcsService.class, new LogDto.NoticeCache(
                "academic.cache.complete",
                !isMiss[0],
                latency
        ));

        return schedules;
    }

    private List<Event> getDoDreamEvents() {
        long start = System.currentTimeMillis();
        boolean[] isMiss = {false};

        List<Event> schedules = doDreamNoticesCache.get(KEY_DODREAM, k -> {
                    isMiss[0] = true;
                    return eventRepository.findByStatus(EventStatus.OPEN);
                }
        );
        long latency = System.currentTimeMillis() - start;

        JsonLog.debug(IcsService.class, new LogDto.NoticeCache(
                "dodream.cache.complete",
                !isMiss[0],
                latency
        ));

        return schedules;
    }

    private <T> T parsePayload(String payload, Class<T> type, String token, String sourceType) {
        if (payload == null || payload.isBlank()) return null;
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            JsonLog.warn(IcsService.class, new LogDto.PayloadParseWarn(
                    "subscription.payload.parse.warn",
                    sourceType,
                    type.getSimpleName(),
                    token,
                    e.getOriginalMessage()
            ));
            return null;
        }
    }

    private boolean passesAcademicFilter(AcademicSchedule schedule, IssueAcademicIcsRequest filter) {
        if (filter == null || filter.selectedGradeId() == null) return true;
        String gradeId = schedule.getGradeId();
        if (gradeId == null) return true; // 전체 학년 일정은 항상 포함
        return gradeId.equals(String.valueOf(filter.selectedGradeId()));
    }

    private boolean passesDoDreamFilter(Event event, IssueDoDreamIcsRequest filter) {
        if (filter == null) return true;
        String deptId = event.getDepartmentId();
        boolean isGlobalDept = (deptId == null || "all".equals(deptId));

        // 학과 필터: "all"(전체)로 분류된 이벤트는 학과 조건에서만 항상 포함하되,
        // 키워드 필터는 아래에서 그대로 적용한다.
        if (filter.selectedDepartmentId() != null) {
            if (!isGlobalDept && !deptId.equals(filter.selectedDepartmentId())) return false;
        }
        if (filter.selectedMinorDepartmentId() != null) {
            if (!isGlobalDept && !deptId.equals(filter.selectedMinorDepartmentId())) return false;
        }

        List<String> selectedKeywordIds = filter.selectedKeywordId();
        if (selectedKeywordIds != null && !selectedKeywordIds.isEmpty()) {
            List<String> eventKeywords = event.getKeywordIds();
            if (eventKeywords == null || eventKeywords.isEmpty()) return false;
            boolean match = eventKeywords.stream().anyMatch(selectedKeywordIds::contains);
            if (!match) return false;
        }
        return true;
    }

    private void appendAcademicEvent(StringBuilder sb, Subscription subscription, AcademicSchedule n, String now) {
        String uid = "academic-" + n.getId() + "@doogoo";
        LocalDateTime start = n.getStartDate().atStartOfDay();
        LocalDateTime end = n.getEndDate().atTime(23, 59);
        String startStr = ICS_SEOUL.format(start.atZone(SEOUL));
        String endStr = ICS_SEOUL.format(end.atZone(SEOUL));
        int alarmMin = subscription.getAlarmMinutesBefore() != null ? subscription.getAlarmMinutesBefore() : DEFAULT_ALARM_MINUTES;

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART;TZID=Asia/Seoul:").append(startStr).append("\r\n");
        sb.append("DTEND;TZID=Asia/Seoul:").append(endStr).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText((n.getContent() != null ? n.getContent() : ""))).append("\r\n");
        sb.append("DESCRIPTION:").append(escape("학사 공지")).append("\r\n");
        if (subscription.isAlarmEnabled()) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(alarmMin).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder\r\n");
            sb.append("END:VALARM\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private void appendDoDreamEvent(StringBuilder sb, Subscription subscription, Event event, String now) {
        String uid = "doodream-" + event.getDodreamId() + "@doogoo";
        int alarmMin = subscription.getAlarmMinutesBefore() != null ? subscription.getAlarmMinutesBefore() : DEFAULT_ALARM_MINUTES;

        LocalDateTime operateStart = event.getOperateStart();
        LocalDateTime operateEnd = event.getOperateEnd();

        LocalDateTime applyStart = event.getApplyStart();
        LocalDateTime applyEnd = event.getApplyEnd();

        LocalDateTime base = applyStart != null ? applyStart : operateStart;
        if (base == null) return;

        LocalDateTime start = base.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        String startStr = start.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE);
        String endStr = end.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE);

        String desc = "신청기간:" + dateFormat(applyStart, applyEnd) + "\n"
                + "운영기간:" + dateFormat(operateStart, operateEnd) + "\n"
                + (event.getDescriptionSummary() != null ? event.getDescriptionSummary() : "");

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART;VALUE=DATE:").append(startStr).append("\r\n");
        sb.append("DTEND;VALUE=DATE:").append(endStr).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText(shortenTitle(event.getTitle()))).append("\r\n");
        sb.append("URL:").append(escape(event.getDodreamUrl() != null ? event.getDodreamUrl() : "")).append("\r\n");
        sb.append("DESCRIPTION:").append(escapeIcsText(desc)).append("\r\n");
        if (subscription.isAlarmEnabled()) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(alarmMin).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder\r\n");
            sb.append("END:VALARM\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private String dateFormat(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return "추후 공지";
        }
        if (start != null && end == null) {
            return start.format(DATE_FORMAT);
        }

        if (start == null) {
            return "~ " + end.format(DATE_FORMAT);
        }
        return start.format(DATE_FORMAT) + " ~ " + end.format(DATE_FORMAT);
    }

    private String shortenTitle(String title) {
        if (title == null) return "";

        title = title.trim();

        String shortened = title.replaceFirst("^\\[.*?\\]\\s*\\d{2,4}(?:학년도\\s*)?(?:[12](?:학기)?|-[12](?:학기)?)\\s*", "").trim();

        return shortened.isBlank() ? title : shortened;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return SPECIAL.matcher(s).replaceAll("\\\\$1");
    }

    private static String escapeIcsText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }

    /**
     * RFC 5545: 75 octet 초과 라인을 CRLF + 공백으로 접어서 반환
     */
    private static String fold(String ics) {
        StringBuilder result = new StringBuilder();
        int start = 0;
        while (start < ics.length()) {
            int end = ics.indexOf("\r\n", start);
            if (end == -1) {
                result.append(foldLine(ics.substring(start)));
                break;
            }
            result.append(foldLine(ics.substring(start, end)));
            start = end + 2;
        }
        return result.toString();
    }

    private static String foldLine(String line) {
        byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= 75) {
            return line + "\r\n";
        }
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        boolean first = true;
        while (pos < bytes.length) {
            int limit = first ? 75 : 74; // 접힌 줄은 앞에 공백 1바이트
            int end = Math.min(pos + limit, bytes.length);
            // 멀티바이트 UTF-8 문자 중간에서 자르지 않도록 조정
            while (end < bytes.length && (bytes[end] & 0xC0) == 0x80) end--;
            if (!first) sb.append(' ');
            sb.append(new String(bytes, pos, end - pos, StandardCharsets.UTF_8));
            pos = end;
            if (pos < bytes.length) sb.append("\r\n");
            first = false;
        }
        sb.append("\r\n");
        return sb.toString();
    }

}
