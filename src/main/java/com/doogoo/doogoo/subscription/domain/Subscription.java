package com.doogoo.doogoo.subscription.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(name = "uk_subscription_token", columnNames = "token"),
        indexes = {
                @Index(name = "idx_subscription_filter_hash", columnList = "filterHash"),
                @Index(name = "idx_subscription_enabled_last_accessed", columnList = "enabled, lastAccessedAt"),
                @Index(name = "idx_subscription_enabled_created", columnList = "enabled, createdAt")
        }
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

    @Column(nullable = false, length = 64, updatable = false)
    private String filterHash;

    protected Subscription() {
    }

    public Subscription(String token, SourceType sourceType, String payload, boolean alarmEnabled, Integer alarmMinutesBefore, String filterHash) {
        if (token == null || token.isBlank()) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        if (sourceType == null) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        if (payload == null || payload.isBlank()) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        if (alarmEnabled && (alarmMinutesBefore == null || alarmMinutesBefore < 0 || alarmMinutesBefore > 10080))
            throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        if (filterHash == null || filterHash.isBlank()) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        this.token = token;
        this.sourceType = sourceType;
        this.payload = payload;
        this.alarmEnabled = alarmEnabled;
        this.alarmMinutesBefore = alarmEnabled ? alarmMinutesBefore : null;
        this.createdAt = Instant.now();
        this.enabled = true;
        this.filterHash = filterHash;
    }

    public Long getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getPayload() {
        return payload;
    }

    public boolean isAlarmEnabled() {
        return alarmEnabled;
    }

    public Integer getAlarmMinutesBefore() {
        return alarmMinutesBefore;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public String getFilterHash() {
        return filterHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void touch() {
        this.lastAccessedAt = Instant.now();
    }

    public void disable() {
        this.enabled = false;
    }
}
