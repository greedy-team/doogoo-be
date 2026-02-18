package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.subscription.domain.Subscription;
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
                .orElseThrow(() -> new DoogooException(ErrorCode.TOKEN_NOT_FOUND));
    }
}
