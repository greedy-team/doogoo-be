package com.doogoo.doogoo.crawl;

import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.dodream.domain.EventDto;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import com.doogoo.doogoo.global.config.CrawlConfig;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DodreamParser {

    private static final Logger log = LoggerFactory.getLogger(DodreamParser.class);

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{4})\\.(\\d{2})\\.(\\d{2})\\([^)]+\\)(?:\\s+(\\d{2}:\\d{2}))?"
    );

    private final CrawlConfig config;

    public DodreamParser(CrawlConfig config) {
        this.config = config;
    }

    public List<EventDto> parseList(Document doc) {
        List<EventDto> events = new ArrayList<>();
        Elements items = doc.select("ul[data-role=list] [data-role=item]");

        for (Element item : items) {
            try {
                if (item.hasClass("CLOSED") || item.selectFirst("label.CLOSED") != null) {
                    continue;
                }

                Element link = item.selectFirst("a[data-idx]");
                if (link == null) {
                    continue;
                }

                Long dodreamId = Long.parseLong(link.attr("data-idx"));

                Element titleEl = item.selectFirst(".title");
                String title = titleEl != null ? titleEl.text() : "";

                Element deptEl = item.selectFirst("div.content > label");
                String departmentText = deptEl != null ? deptEl.text().trim() : "";
                String department = departmentText.isBlank() ? "전체" : departmentText;

                LocalDateTime applyStart = null;
                LocalDateTime applyEnd = null;
                LocalDateTime operateStart = null;
                LocalDateTime operateEnd = null;

                Elements smalls = item.select("div.content small");
                for (Element small : smalls) {
                    String text = small.text();
                    if (text.startsWith("신청:") || text.startsWith("신청 :")) {
                        String dateText = text.replaceFirst("신청\\s*:\\s*", "");
                        LocalDateTime[] dates = parseDateRange(dateText);
                        applyStart = dates[0];
                        applyEnd = dates[1];
                    } else if (text.startsWith("운영:") || text.startsWith("운영 :")) {
                        String dateText = text.replaceFirst("운영\\s*:\\s*", "");
                        LocalDateTime[] dates = parseDateRange(dateText);
                        operateStart = dates[0];
                        operateEnd = dates[1];
                    }
                }

                String dodreamUrl = config.buildDetailUrl(dodreamId);

                EventDto dto = EventDto.builder()
                        .dodreamId(dodreamId)
                        .title(title)
                        .department(department)
                        .applyStart(applyStart)
                        .applyEnd(applyEnd)
                        .operateStart(operateStart)
                        .operateEnd(operateEnd)
                        .location(null)
                        .dodreamUrl(dodreamUrl)
                        .status(EventStatus.OPEN)
                        .build();
                events.add(dto);
            } catch (Exception e) {
                log.warn("[{}] {}: {}", ErrorCode.INVALID_HTML_STRUCTURE.getCode(), ErrorCode.INVALID_HTML_STRUCTURE.getMessage(), e.getMessage(), e);
            }
        }

        return events;
    }

    public EventDto parseDetail(Document doc, EventDto listData) {
        // 설명 파싱
        Element wysiwygEl = doc.selectFirst("[data-role=wysiwyg-content]");
        String description = wysiwygEl != null ? wysiwygEl.text() : "";

        // 장소 파싱 (없으면 null)
        String location = parseLocation(doc);

        // 운영 날짜 파싱
        LocalDateTime operateStart = listData.operateStart();
        LocalDateTime operateEnd = listData.operateEnd();

        Elements timeElements = doc.select("form[data-role=topic] time[datetime]");
        if (timeElements.size() >= 2) {
            LocalDateTime start = parseIsoDateTime(timeElements.get(0).attr("datetime"));
            LocalDateTime end = parseIsoDateTime(timeElements.get(1).attr("datetime"));
            if (start != null) operateStart = start;
            if (end != null) operateEnd = end;
        } else if (timeElements.size() == 1) {
            LocalDateTime start = parseIsoDateTime(timeElements.get(0).attr("datetime"));
            if (start != null) operateStart = start;
        }

        return EventDto.builder(listData)
                .description(description)
                .location(location)
                .operateStart(operateStart)
                .operateEnd(operateEnd)
                .build();
    }

    public List<Long> parseClosedIds(Document doc) {
        List<Long> closedIds = new ArrayList<>();
        Elements items = doc.select("ul[data-role=list] [data-role=item]");

        for (Element item : items) {
            if (item.hasClass("CLOSED") || item.selectFirst("label.CLOSED") != null) {
                Element link = item.selectFirst("a[data-idx]");
                if (link != null) {
                    try {
                        closedIds.add(Long.parseLong(link.attr("data-idx")));
                    } catch (Exception e) {
                        log.debug("[{}] {}: {}", ErrorCode.INVALID_HTML_STRUCTURE.getCode(), ErrorCode.INVALID_HTML_STRUCTURE.getMessage(), link.attr("data-idx"), e);
                    }
                }
            }
        }

        return closedIds;
    }

    /**
     * 상세 페이지에서 장소 정보를 파싱합니다.
     * 장소 필드가 없으면 null을 반환합니다.
     */
    private String parseLocation(Document doc) {
        // th/dt 에서 "장소" 레이블을 찾아 인접 값 추출 시도
        Element locationLabel = doc.selectFirst("th:contains(장소), dt:contains(장소)");
        if (locationLabel != null) {
            Element valueEl = locationLabel.nextElementSibling();
            if (valueEl != null) {
                String text = valueEl.text().trim();
                return text.isEmpty() ? null : text;
            }
        }
        return null;
    }

    private LocalDateTime[] parseDateRange(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        LocalDateTime start = null;
        LocalDateTime end = null;

        if (matcher.find()) {
            start = buildDateTime(matcher);
        }
        if (matcher.find()) {
            end = buildDateTime(matcher);
        }

        return new LocalDateTime[]{start, end};
    }

    private LocalDateTime buildDateTime(Matcher matcher) {
        try {
            int year = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int day = Integer.parseInt(matcher.group(3));
            String time = matcher.group(4);

            if (time != null) {
                String[] parts = time.split(":");
                return LocalDateTime.of(year, month, day,
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
            return LocalDateTime.of(year, month, day, 0, 0);
        } catch (Exception e) {
            log.warn("[{}] {}: {}", ErrorCode.PARSE_FAILED.getCode(), ErrorCode.PARSE_FAILED.getMessage(), matcher.group(), e);
            return null;
        }
    }

    private LocalDateTime parseIsoDateTime(String datetime) {
        try {
            return OffsetDateTime.parse(datetime).toLocalDateTime();
        } catch (Exception e) {
            log.warn("[{}] {}: {}", ErrorCode.PARSE_FAILED.getCode(), ErrorCode.PARSE_FAILED.getMessage(), datetime, e);
            return null;
        }
    }
}
