package com.doogoo.doogoo.dodream.api;

import com.doogoo.doogoo.dodream.api.dto.DoDreamNoticesResponse;
import com.doogoo.doogoo.dodream.api.dto.IssueDoDreamIcsRequest;
import com.doogoo.doogoo.dodream.api.dto.IssueIcsResponse;
import com.doogoo.doogoo.dodream.application.DoDreamNoticeQueryService;
import com.doogoo.doogoo.subscription.application.SubscriptionIssueService;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/dodream")
public class DoDreamController {

    private final SubscriptionIssueService subscriptionIssueService;
    private final DoDreamNoticeQueryService doDreamNoticeQueryService;

    public DoDreamController(SubscriptionIssueService subscriptionIssueService, DoDreamNoticeQueryService doDreamNoticeQueryService) {
        this.subscriptionIssueService = subscriptionIssueService;
        this.doDreamNoticeQueryService = doDreamNoticeQueryService;
    }

    @GetMapping("/notices")
    public DoDreamNoticesResponse getNotices() {
        return doDreamNoticeQueryService.getNotices();
    }

    @PostMapping("/ics")
    public IssueIcsResponse issueIcs(@RequestBody IssueDoDreamIcsRequest request) {
        Subscription subscription = subscriptionIssueService.issue(
                SourceType.DODREAM,
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
