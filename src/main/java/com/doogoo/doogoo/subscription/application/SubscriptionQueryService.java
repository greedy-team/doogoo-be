package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.subscription.domain.Subscription;
import com.doogoo.doogoo.subscription.domain.SubscriptionNotFoundException;
import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionQueryService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionQueryService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription getByToken(String token) {
        return subscriptionRepository.findByToken(token)
                .orElseThrow(SubscriptionNotFoundException::new);
    }
}
