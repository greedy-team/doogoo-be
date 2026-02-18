package com.doogoo.doogoo.calendar.api;

import com.doogoo.doogoo.calendar.application.IcsService;
import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.subscription.application.SubscriptionQueryService;
import com.doogoo.doogoo.subscription.domain.Subscription;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CalendarController {

    private static final MediaType TEXT_CALENDAR = MediaType.parseMediaType("text/calendar; charset=utf-8");
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,64}$");

    private final SubscriptionQueryService subscriptionQueryService;
    private final IcsService icsService;

    public CalendarController(SubscriptionQueryService subscriptionQueryService, IcsService icsService) {
        this.subscriptionQueryService = subscriptionQueryService;
        this.icsService = icsService;
    }

    @GetMapping(value = "/cal/{token}.ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<String> getIcs(
            @PathVariable("token") String token,
            @RequestParam(name = "download", required = false, defaultValue = "false") boolean download
    ) {
        if (token == null || !TOKEN_PATTERN.matcher(token).matches()) {
            throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        }
        Subscription subscription = subscriptionQueryService.getByToken(token);
        subscription.touch();
        String body = icsService.render(subscription);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(TEXT_CALENDAR);
        if (download) {
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"schedule.ics\"");
        }
        return ResponseEntity.ok().headers(headers).body(body);
    }
}
