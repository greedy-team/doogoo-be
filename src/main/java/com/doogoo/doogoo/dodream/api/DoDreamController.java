package com.doogoo.doogoo.dodream.api;

import com.doogoo.doogoo.common.error.ErrorResponse;
import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
import com.doogoo.doogoo.calendar.application.IcsService;
import com.doogoo.doogoo.dodream.api.dto.DoDreamNoticesResponse;
import com.doogoo.doogoo.dodream.api.dto.IssueDoDreamIcsRequest;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.dodream.application.DoDreamNoticeQueryService;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(name = "DoDream", description = "두드림 공지·ICS")
@RestController
@RequestMapping("/api/dodream")
public class DoDreamController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final DoDreamNoticeQueryService doDreamNoticeQueryService;
    private final IcsService icsService;

    public DoDreamController(SubscriptionIssueService subscriptionIssueService, DoDreamNoticeQueryService doDreamNoticeQueryService, IcsService icsService) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.doDreamNoticeQueryService = doDreamNoticeQueryService;
        this.icsService = icsService;
    }

    @Operation(summary = "두드림 공지 목록")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/notices")
    public DoDreamNoticesResponse getNotices() {
        return doDreamNoticeQueryService.getNotices();
    }

    @Operation(summary = "두드림 공지 필터 미리보기", description = "ICS 발급 전에 실제 달력에 포함될 두드림 공지 목록을 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/notices/filter")
    public DoDreamNoticesResponse previewFilteredNotices(@RequestBody IssueDoDreamIcsRequest request) {
        return icsService.previewDoDreamNotices(request);
    }

    @Operation(summary = "두드림 ICS 발급", description = "구독 토큰·icsUrl·downloadUrl 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueDoDreamIcsRequest request) {
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.DODREAM,
                request,
                false,
                null
        );

        JsonLog.info(DoDreamController.class, new LogDto.IcsIssueLog(
                "ics.issue.success",
                SourceType.DODREAM.name(),
                subscription.getToken()
        ));

        String baseUrl = buildPublicBaseUrl();
        String icsUrl = baseUrl + "/cal/" + subscription.getToken() + ".ics";
        String downloadUrl = icsUrl + "?download=true";
        return new IssueIcsResponse(subscription.getToken(), icsUrl, downloadUrl);
    }

    private String buildPublicBaseUrl() {
        var builder = ServletUriComponentsBuilder.fromCurrentContextPath();
        var components = builder.build();

        boolean isExternalHost = components.getHost() != null
                && !"localhost".equals(components.getHost())
                && !"127.0.0.1".equals(components.getHost());

        // 운영 환경에서는 공개 URL을 https://host:50018 로 고정한다.
        if (isExternalHost) {
            builder.scheme("https");
        }

        return builder.build().toUriString();
    }
}
