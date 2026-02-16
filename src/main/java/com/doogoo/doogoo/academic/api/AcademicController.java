package com.doogoo.doogoo.academic.api;

import com.doogoo.doogoo.academic.api.dto.IssueAcademicIcsRequest;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public AcademicController(SubscriptionIssueService subscriptionIssueService, ObjectMapper objectMapper) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.objectMapper = objectMapper;
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
