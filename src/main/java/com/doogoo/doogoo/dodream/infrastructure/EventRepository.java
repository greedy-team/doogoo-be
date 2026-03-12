package com.doogoo.doogoo.dodream.infrastructure;

import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByDodreamId(Long dodreamId);

    List<Event> findByStatus(EventStatus status);

    @Modifying
    @Query("UPDATE Event e SET e.status = 'CLOSED', e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.dodreamId IN :dodreamIds AND e.status = 'OPEN'")
    int markClosedByDodreamIds(@Param("dodreamIds") List<Long> dodreamIds);

    @Modifying
    @Query("DELETE FROM Event e WHERE e.operateEnd IS NOT NULL AND e.operateEnd < :now")
    int deleteExpiredEvents(@Param("now") LocalDateTime now);
}
