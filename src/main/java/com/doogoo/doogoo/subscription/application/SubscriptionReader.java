package com.doogoo.doogoo.subscription.application;

import com.doogoo.doogoo.subscription.domain.Subscription;

public interface SubscriptionReader {
    public Subscription getByToken(String token);
}
