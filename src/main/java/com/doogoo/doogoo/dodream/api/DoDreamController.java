package com.doogoo.doogoo.dodream.api;

import com.doogoo.doogoo.dodream.api.dto.IssueDoDreamIcsRequest;
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
@RequestMapping("/api/dodream")
public class DoDreamController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final ObjectMapper objectMapper;

    public DoDreamController(SubscriptionIssueService subscriptionIssueService, ObjectMapper objectMapper) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueDoDreamIcsRequest request) throws JsonProcessingException {
        String payload = objectMapper.writeValueAsString(request);
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.DODREAM,
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
