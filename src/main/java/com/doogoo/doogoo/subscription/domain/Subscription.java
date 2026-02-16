package com.doogoo.doogoo.subscription.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(name = "uk_subscription_token", columnNames = "token")
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean alarmEnabled;

    private Integer alarmMinutesBefore;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant lastAccessedAt;

    @Column(nullable = false)
    private boolean enabled;

    protected Subscription() {}

    public Subscription(String token, SourceType sourceType, String payload, boolean alarmEnabled, Integer alarmMinutesBefore) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException();
        if (sourceType == null) throw new IllegalArgumentException();
        if (payload == null || payload.isBlank()) throw new IllegalArgumentException();
        if (alarmEnabled && (alarmMinutesBefore == null || alarmMinutesBefore < 0 || alarmMinutesBefore > 10080))
            throw new IllegalArgumentException();
        this.token = token;
        this.sourceType = sourceType;
        this.payload = payload;
        this.alarmEnabled = alarmEnabled;
        this.alarmMinutesBefore = alarmEnabled ? alarmMinutesBefore : null;
        this.createdAt = Instant.now();
        this.enabled = true;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public SourceType getSourceType() { return sourceType; }
    public String getPayload() { return payload; }
    public boolean isAlarmEnabled() { return alarmEnabled; }
    public Integer getAlarmMinutesBefore() { return alarmMinutesBefore; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastAccessedAt() { return lastAccessedAt; }
    public boolean isEnabled() { return enabled; }

    public void touch() {
        this.lastAccessedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
    }
}
