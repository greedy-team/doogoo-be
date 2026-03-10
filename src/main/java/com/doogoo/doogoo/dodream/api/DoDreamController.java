package com.doogoo.doogoo.dodream.api;

import com.doogoo.doogoo.common.error.ErrorResponse;
import com.doogoo.doogoo.common.log.JsonLog;
import com.doogoo.doogoo.common.log.LogDto;
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

    public DoDreamController(SubscriptionIssueService subscriptionIssueService, DoDreamNoticeQueryService doDreamNoticeQueryService) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.doDreamNoticeQueryService = doDreamNoticeQueryService;
    }

    @Operation(summary = "두드림 공지 목록")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/notices")
    public DoDreamNoticesResponse getNotices() {
        return doDreamNoticeQueryService.getNotices();
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

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String icsUrl = baseUrl + "/cal/" + subscription.getToken() + ".ics";
        String downloadUrl = icsUrl + "?download=true";
        return new IssueIcsResponse(subscription.getToken(), icsUrl, downloadUrl);
    }
}
