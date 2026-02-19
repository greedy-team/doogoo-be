package com.doogoo.doogoo.academic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AcademicScheduleRepository extends JpaRepository<AcademicSchedule, Long> {

    @Modifying
    @Query("DELETE FROM AcademicSchedule a WHERE a.year = :year")
    void deleteAllByYear(int year);
}
