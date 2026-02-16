package com.doogoo.doogoo.subscription.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = @UniqueConstraint(name = "uk_subscription_token", columnNames = "token")
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SourceType sourceType;

    @Column(nullable = false)
    private String payload;

    protected Subscription() {}

    public Subscription(String token, SourceType sourceType, String payload) {
        if (token == null || token.isBlank()) throw new IllegalArgumentException();
        if (sourceType == null) throw new IllegalArgumentException();
        if (payload == null || payload.isBlank()) throw new IllegalArgumentException();
        this.token = token;
        this.sourceType = sourceType;
        this.payload = payload;
    }

    public Long getId() { return id; }
    public String getToken() { return token; }
    public SourceType getSourceType() { return sourceType; }
    public String getPayload() { return payload; }
}
