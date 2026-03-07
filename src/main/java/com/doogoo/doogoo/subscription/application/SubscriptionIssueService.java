package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.common.util.Sha256;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

@Service
public class SubscriptionIssueService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TOKEN_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public SubscriptionIssueService(SubscriptionRepository subscriptionRepository, @Qualifier("canonicalObjectMapper") ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    public Subscription issue(SourceType sourceType, Object payload, boolean alarmEnabled, Integer alarmMinutesBefore) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            return issue(sourceType, payloadJson, alarmEnabled, alarmMinutesBefore);
        } catch (JacksonException e) {
            throw new DoogooException(ErrorCode.INTERNAL_SERVER_ERROR, e);
        }
    }

    public Subscription issue(SourceType sourceType, String payload, boolean alarmEnabled, Integer alarmMinutesBefore) {
        String token;
        do {
            token = generateBase62Token();
        } while (subscriptionRepository.existsByToken(token));
        Integer newAlarmMinutes = alarmEnabled ? alarmMinutesBefore : null;
        String filterHash = Sha256.sha256(sourceType.name() + "|"
                + payload + "|"
                + "alarmEnabled=" + alarmEnabled + "|"
                + "alarmMinutesBefore=" + newAlarmMinutes);
        Subscription subscription = new Subscription(token, sourceType, payload, alarmEnabled, newAlarmMinutes, filterHash);
        return subscriptionRepository.save(subscription);
    }

    private String generateBase62Token() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(TOKEN_LENGTH);
        for (byte b : bytes) {
            sb.append(BASE62.charAt(Math.floorMod(b, BASE62.length())));
        }
        return sb.toString();
    }
}
