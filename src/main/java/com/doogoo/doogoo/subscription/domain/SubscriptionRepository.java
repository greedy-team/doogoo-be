package com.doogoo.doogoo.subscription.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByToken(String token);
    boolean existsByToken(String token);
}
