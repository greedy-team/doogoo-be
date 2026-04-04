package com.doogoo.doogoo.common.log;

public class LogDto {
    public record IcsRequestStart(
            String event,
            String token,
            String userAgent
    ) {
    }

    public record IcsRequestComplete(
            String event,
            String token,
            String userAgent,
            Integer status,
            Long latencyMs,
            Double avgLatencyMs,
            Long requestCount
    ) {
    }

    public record ErrorLog(
            String event,
            String refKey,
            Integer status,
            String errorCode,
            String message
    ) {
    }

    public record IcsRenderLog(
            String event,
            String token,
            String sourceType,
            String filterHash,
            Integer eventCount,
            Long latencyMs,
            Long pureRenderLatencyMs,
            Integer queueLength,
            String riskLevel
    ) {
    }

    public record IcsCache(
            String event,
            String token,
            String filterHash,
            Boolean cacheHit,
            Long latencyMs
    ) {
    }

    public record NoticeCache(
            String event,
            Boolean cacheHit,
            Long latencyMs
    ) {
    }

    public record CacheInvalidateLog(
            String event,
            String sourceType,
            Integer noticeCacheSize,
            Integer removedIcsCacheCount
    ) {
    }

    public record PayloadParseWarn(
            String event,
            String sourceType,
            String payloadType,
            String token,
            String message
    ) {
    }

    public record IcsIssueLog(
            String event,
            String sourceType,
            String token
    ) {
    }

    public record CrawlStartLog(
            String event,
            String domain,
            Integer regularPages,
            Integer activeStatusCount
    ) {
    }

    public record CrawlStatusLog(
            String event,
            String domain,
            String status,
            Integer page
    ) {
    }

    public record CrawlDiscoveredLog(
            String event,
            String domain,
            Integer uniqueCount
    ) {
    }

    public record RegularCrawlSummaryLog(
            String event,
            String domain,
            Integer newCount,
            Integer updateCount,
            Integer closedCount,
            Integer uniqueCount,
            Long latencyMs
    ) {
    }

    public record FullSyncSummaryLog(
            String event,
            String domain,
            Integer syncedCount,
            Integer skipAiCount,
            Integer targetCount,
            Long latencyMs
    ) {
    }

    public record AcademicCrawlSummaryLog(
            String event,
            String domain,
            Integer year,
            Integer scheduleCount,
            Long latencyMs
    ) {
    }

    public record CleanupSummaryLog(
            String event,
            String domain,
            Integer closedCount,
            Long latencyMs
    ) {
    }
}
