package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionIssueService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TOKEN_LENGTH = 12;

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionIssueService(SubscriptionRepository subscriptionRepository, ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    public Subscription issue(SourceType sourceType, Object payload, boolean alarmEnabled, Integer alarmMinutesBefore) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            return issue(sourceType, payloadJson, alarmEnabled, alarmMinutesBefore);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public Subscription issue(SourceType sourceType, String payload, boolean alarmEnabled, Integer alarmMinutesBefore) {
        String token;
        do {
            token = generateBase62Token();
        } while (subscriptionRepository.existsByToken(token));

        Subscription subscription = new Subscription(token, sourceType, payload, alarmEnabled, alarmMinutesBefore);
        return subscriptionRepository.save(subscription);
    }

    private String generateBase62Token() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[TOKEN_LENGTH];
        random.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (byte b : bytes) {
            sb.append(BASE62.charAt(Math.floorMod(b, BASE62.length())));
        }
        return sb.toString();
    }
}
