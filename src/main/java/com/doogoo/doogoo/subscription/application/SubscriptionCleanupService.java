package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SubscriptionCleanupService {
    private final SubscriptionRepository  subscriptionRepository;

    public SubscriptionCleanupService(SubscriptionRepository subscriptionRepository){
        this.subscriptionRepository = subscriptionRepository;
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Seoul")
    @Transactional
    public void disableSubscription(){
        Instant inactiveBefore = Instant.now().minus(15, ChronoUnit.DAYS);
        subscriptionRepository.disableInactive(inactiveBefore);
    }
}
