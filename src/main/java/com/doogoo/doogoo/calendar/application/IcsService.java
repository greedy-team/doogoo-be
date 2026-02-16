package com.doogoo.doogoo.calendar.application;

import com.doogoo.doogoo.subscription.domain.Subscription;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class IcsService {

    private static final DateTimeFormatter ICS_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);
    private static final Pattern SPECIAL = Pattern.compile("([\\\\,;])");

    public String render(Subscription subscription) {
        String now = ICS_UTC.format(Instant.now());
        String uid = "doogoo-" + subscription.getId() + "@doogoo";
        String desc = escape(subscription.getPayload());

        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//Doogoo//EN\r\n");
        sb.append("BEGIN:VEVENT\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("DTSTAMP:").append(now).append("\r\n");
        sb.append("DTSTART:").append(now).append("\r\n");
        sb.append("SUMMARY:Doogoo Schedule\r\n");
        sb.append("DESCRIPTION:").append(desc).append("\r\n");
        if (subscription.isAlarmEnabled() && subscription.getAlarmMinutesBefore() != null) {
            sb.append("BEGIN:VALARM\r\n");
            sb.append("TRIGGER:-PT").append(subscription.getAlarmMinutesBefore()).append("M\r\n");
            sb.append("ACTION:DISPLAY\r\n");
            sb.append("DESCRIPTION:Reminder\r\n");
            sb.append("END:VALARM\r\n");
        }
        sb.append("END:VEVENT\r\n");
        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return SPECIAL.matcher(s).replaceAll("\\\\$1");
    }
}
