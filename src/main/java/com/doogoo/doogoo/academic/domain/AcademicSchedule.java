package com.doogoo.doogoo.academic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "academic_schedule",
        uniqueConstraints = @UniqueConstraint(columnNames = {"schedule_year", "start_date", "content"})
)
public class AcademicSchedule {

    public static final String ALL_GRADE_ID = "all";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_year", nullable = false)
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private String content;

    private String gradeId;  // "all" = 전체, "1"~"4" = 해당 학년만

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected AcademicSchedule() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static AcademicSchedule create(int year, LocalDate startDate, LocalDate endDate, String content, String gradeId) {
        AcademicSchedule s = new AcademicSchedule();
        s.year = year;
        s.startDate = startDate;
        s.endDate = endDate;
        s.content = content;
        s.gradeId = normalizeGradeId(gradeId);
        return s;
    }

    private static String normalizeGradeId(String gradeId) {
        return gradeId == null ? ALL_GRADE_ID : gradeId;
    }

    public Long getId() { return id; }
    public int getYear() { return year; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getContent() { return content; }
    public String getGradeId() { return gradeId; }
}
