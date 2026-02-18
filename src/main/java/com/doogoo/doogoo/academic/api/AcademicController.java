package com.doogoo.doogoo.academic.api;

import com.doogoo.doogoo.academic.api.dto.AcademicNoticesResponse;
import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.academic.application.AcademicNoticeQueryService;
import com.doogoo.doogoo.common.url.PublicBaseUrlResolver;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final AcademicNoticeQueryService academicNoticeQueryService;
    private final PublicBaseUrlResolver publicBaseUrlResolver;

    public AcademicController(SubscriptionIssueService subscriptionIssueService, AcademicNoticeQueryService academicNoticeQueryService, PublicBaseUrlResolver publicBaseUrlResolver) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.academicNoticeQueryService = academicNoticeQueryService;
        this.publicBaseUrlResolver = publicBaseUrlResolver;
    }

    @GetMapping("/notices")
    public AcademicNoticesResponse getNotices() {
        return academicNoticeQueryService.getNotices();
    }

    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueAcademicIcsRequest request, HttpServletRequest httpRequest) {
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.ACADEMIC,
                request,
                request.alarmEnabled(),
                request.alarmMinutesBefore()
        );
        String baseUrl = publicBaseUrlResolver.resolveBaseUrl(httpRequest);
        String icsUrl = baseUrl + "/cal/" + subscription.getToken() + ".ics";
        String downloadUrl = icsUrl + "?download=true";
        return new IssueIcsResponse(subscription.getToken(), icsUrl, downloadUrl);
    }
}
