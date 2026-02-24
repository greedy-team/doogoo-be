package com.doogoo.doogoo.calendar.application;

import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.academic.domain.AcademicNotice;
import com.doogoo.doogoo.academic.infrastructure.AcademicNoticeRepository;
import com.doogoo.doogoo.catalog.domain.Keyword;
import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.dodream.api.dto.IssueDoDreamIcsRequest;
import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import com.doogoo.doogoo.dodream.infrastructure.DoDreamNoticeRepository;
import com.doogoo.doogoo.subscription.application.SubscriptionReader;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
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
    private static final Pattern SPECIAL = Pattern.compile("([\\\\,;])");
    private static final int DEFAULT_ALARM_MINUTES = 60;
    private static final String KEY_ACADEMIC = "ACADEMIC";
    private static final String KEY_DODREAM = "DODREAM";

    private final AcademicNoticeRepository academicNoticeRepository;
    private final DoDreamNoticeRepository doDreamNoticeRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionReader subscriptionReader;
    private final ObjectMapper objectMapper;
    private final Cache<String, String> icsCache;
    private final Cache<String, List<AcademicNotice>> academicNoticesCache;
    private final Cache<String, List<DoDreamNotice>> doDreamNoticesCache;
    private final Semaphore semaphore;

    public IcsService(AcademicNoticeRepository academicNoticeRepository,
                      DoDreamNoticeRepository doDreamNoticeRepository,
                      SubscriptionRepository subscriptionRepository,
                      SubscriptionReader subscriptionReader,
                      ObjectMapper objectMapper,
                      Cache<String, String> icsCache,
                      Cache<String, List<AcademicNotice>> academicNoticesCache,
                      Cache<String, List<DoDreamNotice>> doDreamNoticesCache,
                      Semaphore semaphore) {
        this.academicNoticeRepository = academicNoticeRepository;
        this.doDreamNoticeRepository = doDreamNoticeRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionReader = subscriptionReader;
        this.objectMapper = objectMapper;
        this.icsCache = icsCache;
        this.academicNoticesCache = academicNoticesCache;
        this.doDreamNoticesCache = doDreamNoticesCache;
        this.semaphore = semaphore;
    }

    public String getIcsByToken(String token) {
        Subscription sub = subscriptionReader.getByToken(token);
        touch(token);
        String key = sub.getFilterHash();
        return icsCache.get(key, k -> renderWithLimit(sub));
    }

    private String renderWithLimit(Subscription subscription) {
        boolean acquired = false;
        try {
            acquired = semaphore.tryAcquire(5, TimeUnit.SECONDS);
            if (!acquired) {
                throw new DoogooException(ErrorCode.TOO_MANY_REQUESTS);
            }
            return render(subscription);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (acquired) semaphore.release();
        }
    }

    public void invalidateAcademicDataByUpdate() {
        academicNoticesCache.invalidateAll();
        icsCache.invalidateAll();
    }

    public void invalidateDoDreamDataByUpdate() {
        doDreamNoticesCache.invalidateAll();
        icsCache.invalidateAll();
    }

    private void touch(String token) {
        Instant now = Instant.now();
        Instant thresh = now.minus(8, ChronoUnit.HOURS);
        subscriptionRepository.touchIfStale(token, now, thresh);
    }

    private String render(Subscription subscription) {
        String now = ICS_UTC.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Doogoo//Calendar 1.0//KO\r\n");
        sb.append("CALSCALE:GREGORIAN\r\n");
        sb.append("METHOD:PUBLISH\r\n");

        if (subscription.getSourceType() == SourceType.ACADEMIC) {
            List<AcademicNotice> all = getAcademicNotices();
            IssueAcademicIcsRequest filter = parsePayload(subscription.getPayload(), IssueAcademicIcsRequest.class);
            List<AcademicNotice> filtered = all.stream().filter(n -> passesAcademicFilter(n, filter)).toList();
            if (filtered.isEmpty()) {
                filtered = all;
            }
            for (AcademicNotice n : filtered) {
                appendAcademicEvent(sb, subscription, n, now);
            }
        } else if (subscription.getSourceType() == SourceType.DODREAM) {
            List<DoDreamNotice> all = getDoDreamNotices();
            IssueDoDreamIcsRequest filter = parsePayload(subscription.getPayload(), IssueDoDreamIcsRequest.class);
            List<DoDreamNotice> filtered = all.stream().filter(n -> passesDoDreamFilter(n, filter)).toList();
            if (filtered.isEmpty()) {
                filtered = all;
            }
            for (DoDreamNotice n : filtered) {
                appendDoDreamEvent(sb, subscription, n, now);
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }


    private List<AcademicNotice> getAcademicNotices() {
        return academicNoticesCache.get(KEY_ACADEMIC, k -> academicNoticeRepository.findAll());
    }

    private List<DoDreamNotice> getDoDreamNotices() {
        return doDreamNoticesCache.get(KEY_DODREAM, k -> doDreamNoticeRepository.findAll());
    }

    private <T> T parsePayload(String payload, Class<T> type) {
        if (payload == null || payload.isBlank()) return null;
        try {
            return objectMapper.readValue(payload, type);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private boolean passesAcademicFilter(AcademicNotice notice, IssueAcademicIcsRequest filter) {
        if (filter == null) return true;
        if (filter.selectedGradeId() != null && !notice.getGrade().id().equals(String.valueOf(filter.selectedGradeId()))) {
            return false;
        }
        return true;
    }

    private boolean passesDoDreamFilter(DoDreamNotice notice, IssueDoDreamIcsRequest filter) {
        if (filter == null) return true;
        String dept = notice.getDepartmentName();
        if (dept != null && filter.selectedDepartmentId() != null && !dept.equals(filter.selectedDepartmentId())) {
            return false;
        }
        if (filter.selectedMinorDepartmentId() != null) {
            if (dept == null) return true;
            if (!dept.equals(filter.selectedMinorDepartmentId())) return false;
        }
        List<String> keywordIds = filter.selectedKeywordId();
        if (keywordIds != null && !keywordIds.isEmpty()) {
            Set<Keyword> noticeKeywords = notice.getKeywords();
            if (noticeKeywords == null || noticeKeywords.isEmpty()) return false;
            boolean match = noticeKeywords.stream().anyMatch(k -> keywordIds.contains(k.id()));
            if (!match) return false;
        }
        return true;
    }

    private void appendAcademicEvent(StringBuilder sb, Subscription subscription, AcademicNotice n, String now) {
        String uid = "academic-" + n.getNoticeId() + "@doogoo";
        LocalDateTime startAt = n.getStartAt();
        LocalDateTime endAt = n.getEndAt();
        if (endAt == null && startAt != null) {
            endAt = startAt.plusHours(1);
        }
        String startStr = startAt != null ? ICS_SEOUL.format(startAt.atZone(SEOUL)) : now.replace("Z", "");
        String endStr = endAt != null ? ICS_SEOUL.format(endAt.atZone(SEOUL)) : startStr;
        int alarmMin = subscription.getAlarmMinutesBefore() != null ? subscription.getAlarmMinutesBefore() : DEFAULT_ALARM_MINUTES;

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART;TZID=Asia/Seoul:").append(startStr).append("\r\n");
        sb.append("DTEND;TZID=Asia/Seoul:").append(endStr).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText("[학사] " + (n.getTitle() != null ? n.getTitle() : ""))).append("\r\n");
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

    private void appendDoDreamEvent(StringBuilder sb, Subscription subscription, DoDreamNotice n, String now) {
        String uid = "doodream-" + n.getNoticeId() + "@doogoo";
        LocalDateTime start = n.getOperatingStartAt() != null ? n.getOperatingStartAt() : n.getApplicationStartAt();
        LocalDateTime end = n.getOperatingEndAt() != null ? n.getOperatingEndAt() : n.getApplicationEndAt();
        if (end == null && start != null) end = start.plusHours(1);
        String startStr = start != null ? ICS_SEOUL.format(start.atZone(SEOUL)) : now.replace("Z", "");
        String endStr = end != null ? ICS_SEOUL.format(end.atZone(SEOUL)) : startStr;
        int alarmMin = subscription.getAlarmMinutesBefore() != null ? subscription.getAlarmMinutesBefore() : DEFAULT_ALARM_MINUTES;
        String desc = "두드림 공지";
        if (n.getKeywords() != null && !n.getKeywords().isEmpty()) {
            String first = n.getKeywords().iterator().next().displayName();
            desc = "카테고리: " + first;
        }

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART;TZID=Asia/Seoul:").append(startStr).append("\r\n");
        sb.append("DTEND;TZID=Asia/Seoul:").append(endStr).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText("[두드림] " + (n.getTitle() != null ? n.getTitle() : ""))).append("\r\n");
        sb.append("URL:").append(escape(n.getDetailUrl() != null ? n.getDetailUrl() : "")).append("\r\n");
        sb.append("DESCRIPTION:").append(escape(desc)).append("\r\n");
        if (subscription.isAlarmEnabled()) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(alarmMin).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder\r\n");
            sb.append("END:VALARM\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private static String escape(String s) {
        if (s == null) return "";
        return SPECIAL.matcher(s).replaceAll("\\\\$1");
    }

    private static String escapeIcsText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
    }
}

