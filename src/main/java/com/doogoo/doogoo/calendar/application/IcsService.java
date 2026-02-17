package com.doogoo.doogoo.calendar.application;

import com.doogoo.doogoo.academic.domain.AcademicNotice;
import com.doogoo.doogoo.academic.infrastructure.AcademicNoticeRepository;
import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import com.doogoo.doogoo.dodream.infrastructure.DoDreamNoticeRepository;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class IcsService {

    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final Pattern SPECIAL = Pattern.compile("([\\\\,;])");

    private final AcademicNoticeRepository academicNoticeRepository;
    private final DoDreamNoticeRepository doDreamNoticeRepository;

    public IcsService(AcademicNoticeRepository academicNoticeRepository, DoDreamNoticeRepository doDreamNoticeRepository) {
        this.academicNoticeRepository = academicNoticeRepository;
        this.doDreamNoticeRepository = doDreamNoticeRepository;
    }

    public String render(Subscription subscription) {
        String now = ICS_UTC.format(Instant.now());
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Doogoo//EN\r\n");

        if (subscription.getSourceType() == SourceType.ACADEMIC) {
            List<AcademicNotice> notices = academicNoticeRepository.findAll();
            for (AcademicNotice n : notices) {
                appendAcademicEvent(sb, subscription, n, now);
            }
        } else if (subscription.getSourceType() == SourceType.DODREAM) {
            List<DoDreamNotice> notices = doDreamNoticeRepository.findAll();
            for (DoDreamNotice n : notices) {
                appendDoDreamEvent(sb, subscription, n, now);
            }
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private void appendAcademicEvent(StringBuilder sb, Subscription subscription, AcademicNotice n, String now) {
        String uid = "doogoo-" + subscription.getId() + "-" + n.getNoticeId() + "@doogoo";
        String startUtc = n.getStartAt() != null
                ? ICS_UTC.format(n.getStartAt().atZone(ZoneId.systemDefault()).toInstant())
                : now;
        String endUtc = n.getEndAt() != null
                ? ICS_UTC.format(n.getEndAt().atZone(ZoneId.systemDefault()).toInstant())
                : startUtc;
        String desc = escape(n.getTitle() != null ? n.getTitle() : "");

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART:").append(startUtc).append("\r\n");
        sb.append("DTEND:").append(endUtc).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText(n.getTitle())).append("\r\n");
        sb.append("DESCRIPTION:").append(desc).append("\r\n");
        if (subscription.isAlarmEnabled() && subscription.getAlarmMinutesBefore() != null) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(subscription.getAlarmMinutesBefore()).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder\r\n");
            sb.append("END:VALARM\r\n");
        }
        sb.append("END:VEVENT\r\n");
    }

    private void appendDoDreamEvent(StringBuilder sb, Subscription subscription, DoDreamNotice n, String now) {
        String uid = "doogoo-" + subscription.getId() + "-" + n.getNoticeId() + "@doogoo";
        java.time.LocalDateTime start = n.getOperatingStartAt() != null ? n.getOperatingStartAt() : n.getApplicationStartAt();
        java.time.LocalDateTime end = n.getOperatingEndAt() != null ? n.getOperatingEndAt() : n.getApplicationEndAt();
        String startUtc = start != null
                ? ICS_UTC.format(start.atZone(ZoneId.systemDefault()).toInstant())
                : now;
        String endUtc = end != null
                ? ICS_UTC.format(end.atZone(ZoneId.systemDefault()).toInstant())
                : startUtc;
        String desc = escape(n.getDetailUrl() != null ? n.getDetailUrl() : n.getTitle() != null ? n.getTitle() : "");

        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART:").append(startUtc).append("\r\n");
        sb.append("DTEND:").append(endUtc).append("\r\n");
        sb.append("SUMMARY:").append(escapeIcsText(n.getTitle())).append("\r\n");
        sb.append("DESCRIPTION:").append(desc).append("\r\n");
        if (subscription.isAlarmEnabled() && subscription.getAlarmMinutesBefore() != null) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(subscription.getAlarmMinutesBefore()).append("M\r\n");
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
