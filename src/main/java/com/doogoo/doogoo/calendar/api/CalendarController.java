package com.doogoo.doogoo.calendar.api;

import com.doogoo.doogoo.calendar.application.IcsService;
import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.common.error.ErrorResponse;
import com.doogoo.doogoo.subscription.application.SubscriptionQueryService;
import com.doogoo.doogoo.subscription.domain.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Calendar", description = "ICS 캘린더 조회")
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

    @Operation(summary = "ICS 조회", description = "구독 토큰으로 ICS 캘린더 본문 조회. text/calendar 반환.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공 (text/calendar)"),
            @ApiResponse(responseCode = "400", description = "토큰 형식 오류", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "토큰 없음", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(value = "/cal/{token}.ics", produces = "text/calendar; charset=utf-8")
    public ResponseEntity<String> getIcs(
            @Parameter(description = "구독 토큰 (6~64자 영숫자)", required = true) @PathVariable("token") String token,
            @Parameter(description = "true면 Content-Disposition: attachment") @RequestParam(name = "download", required = false, defaultValue = "false") boolean download
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
