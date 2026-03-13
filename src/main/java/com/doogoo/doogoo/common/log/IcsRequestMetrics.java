package com.doogoo.doogoo.common.log;

import java.util.concurrent.atomic.LongAdder;

import org.springframework.stereotype.Component;

@Component
public class IcsRequestMetrics {
    private final LongAdder totalLatencyMs = new LongAdder();
    private final LongAdder requestCount = new LongAdder();

    public Snapshot record(long latencyMs) {
        totalLatencyMs.add(latencyMs);
        requestCount.increment();

        long count = requestCount.sum();
        long total = totalLatencyMs.sum();
        double avgLatencyMs = count == 0 ? 0D : (double) total / count;

        return new Snapshot(count, avgLatencyMs);
    }

    public record Snapshot(
            long requestCount,
            double avgLatencyMs
    ) {
    }
}
