package com.doogoo.doogoo.academic.infrastructure;

import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AcademicScheduleRepository extends JpaRepository<AcademicSchedule, Long> {

    @Modifying
    @Query("DELETE FROM AcademicSchedule a WHERE a.year = :year")
    void deleteAllByYear(int year);

    List<AcademicSchedule> findByYear(int year);
}
