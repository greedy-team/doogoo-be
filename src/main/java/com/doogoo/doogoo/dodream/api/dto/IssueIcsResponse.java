package com.doogoo.doogoo.dodream.api.dto;

public record IssueIcsResponse(
        String token,
        String icsUrl,
        String downloadUrl
) {}
