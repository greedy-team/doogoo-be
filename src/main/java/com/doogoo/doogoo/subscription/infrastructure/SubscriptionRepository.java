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
            SET s.lastAccessedAt = :time
            WHERE s.token = :token 
            AND s.enabled = true
            """)
    int updateLastAccessedAtByToken(@Param("token") String token, @Param("time") Instant time);

    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE Subscription s
            WHERE s.enabled = true
            AND (
                (s.lastAccessedAt IS NOT NULL AND s.lastAccessedAt < :inactiveBefore)
                OR (s.lastAccessedAt IS NULL AND s.createdAt < :inactiveBefore)
            )
            """)
    int deleteInactive(@Param("inactiveBefore") Instant inactiveBefore);

}
