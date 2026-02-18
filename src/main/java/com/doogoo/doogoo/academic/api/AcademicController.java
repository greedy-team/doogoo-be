package com.doogoo.doogoo.academic.api;

import com.doogoo.doogoo.academic.api.dto.AcademicNoticesResponse;
import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.academic.application.AcademicNoticeQueryService;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Academic", description = "학사 공지 및 ICS 구독")
@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final AcademicNoticeQueryService academicNoticeQueryService;

    public AcademicController(SubscriptionIssueService subscriptionIssueService, AcademicNoticeQueryService academicNoticeQueryService) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.academicNoticeQueryService = academicNoticeQueryService;
    }

    @Operation(summary = "학사 공지 목록 조회")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/notices")
    public AcademicNoticesResponse getNotices() {
        return academicNoticeQueryService.getNotices();
    }

    @Operation(summary = "학사 ICS 구독 발급", description = "선택 조건에 맞는 학사 일정용 구독 토큰을 발급하고 icsUrl·downloadUrl을 반환한다")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청(예: alarmEnabled=true인데 alarmMinutesBefore 없음)")
    })
    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueAcademicIcsRequest request) {
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.ACADEMIC,
                request,
                request.alarmEnabled(),
                request.alarmMinutesBefore()
        );
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String icsUrl = baseUrl + "/cal/" + subscription.getToken() + ".ics";
        String downloadUrl = icsUrl + "?download=true";
        return new IssueIcsResponse(subscription.getToken(), icsUrl, downloadUrl);
    }
}
