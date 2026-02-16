package com.doogoo.doogoo.academic.api;

import com.doogoo.doogoo.academic.api.dto.AcademicNoticesResponse;
import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.academic.application.AcademicNoticeQueryService;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/academic")
public class AcademicController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final ObjectMapper objectMapper;
    private final AcademicNoticeQueryService academicNoticeQueryService;

    public AcademicController(SubscriptionIssueService subscriptionIssueService, ObjectMapper objectMapper, AcademicNoticeQueryService academicNoticeQueryService) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.objectMapper = objectMapper;
        this.academicNoticeQueryService = academicNoticeQueryService;
    }

    @GetMapping("/notices")
    public AcademicNoticesResponse getNotices() {
        return academicNoticeQueryService.getNotices();
    }

    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueAcademicIcsRequest request) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(request);
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.ACADEMIC,
                payload,
                request.alarmEnabled(),
                request.alarmMinutesBefore()
        );
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        String icsUrl = baseUrl + "/cal/" + subscription.getToken() + ".ics";
        String downloadUrl = icsUrl + "?download=true";
        return new IssueIcsResponse(subscription.getToken(), icsUrl, downloadUrl);
    }
}
