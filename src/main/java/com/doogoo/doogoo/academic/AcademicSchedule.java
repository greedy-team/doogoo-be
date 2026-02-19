package com.doogoo.doogoo.academic;

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
        uniqueConstraints = @UniqueConstraint(columnNames = {"year", "department", "start_date", "content"})
)
public class AcademicSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;
    private String department;
    private LocalDate startDate;
    private LocalDate endDate;

    @Column(nullable = false)
    private String content;

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

    public static AcademicSchedule create(int year, String department, LocalDate startDate, LocalDate endDate, String content) {
        AcademicSchedule s = new AcademicSchedule();
        s.year = year;
        s.department = department;
        s.startDate = startDate;
        s.endDate = endDate;
        s.content = content;
        return s;
    }

    public int getYear() { return year; }
    public String getDepartment() { return department; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getContent() { return content; }
}
