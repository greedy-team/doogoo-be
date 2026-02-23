package com.doogoo.doogoo.subscription.infrastructure;

import com.doogoo.doogoo.subscription.domain.Subscription;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByToken(String token);

    boolean existsByToken(String token);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Subscription s
            SET s.lastAccessedAt = :now
            WHERE s.token = :token 
            AND s.enabled = true
            AND (s.lastAccessedAt is null OR s.lastAccessedAt <= :thresh)
            """)
    int touchIfStale(@Param("token") String token,
                     @Param("now") Instant now,
                     @Param("thresh") Instant thresh);

    @Modifying(clearAutomatically = true)
    @Query("""
           UPDATE Subscription s
           SET s.enabled = false
           WHERE s.enabled = true
           AND (s.lastAccessedAt is null OR s.lastAccessedAt < :inactiveBefore)
           """)
    int disableInactive(@Param("inactiveBefore") Instant inactiveBefore);

}
