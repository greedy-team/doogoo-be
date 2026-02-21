package com.doogoo.doogoo.subscription.api.dto;

public record IssueIcsResponse(
        String token,
        String icsUrl,
        String downloadUrl
) {}
