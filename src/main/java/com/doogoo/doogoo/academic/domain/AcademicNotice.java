package com.doogoo.doogoo.academic.domain;

import com.doogoo.doogoo.lookup.domain.Grade;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "academic_notices",
        indexes = {
                @Index(name = "idx_academic_start", columnList = "startAt"),
                @Index(name = "idx_academic_grade", columnList = "grade")
        }
)
public class AcademicNotice {

    @Id
    @Column
    private String noticeId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    protected AcademicNotice() {}

    public AcademicNotice(String noticeId, String title, Grade grade, LocalDateTime startAt, LocalDateTime endAt) {
        this.noticeId = noticeId;
        this.title = title;
        this.grade = grade;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public String getNoticeId() { return noticeId; }
    public String getTitle() { return title; }
    public Grade getGrade() { return grade; }
    public LocalDateTime getStartAt() { return startAt; }
    public LocalDateTime getEndAt() { return endAt; }
}
