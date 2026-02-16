package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionIssueService {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TOKEN_LENGTH = 12;

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionIssueService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
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
