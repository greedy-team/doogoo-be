package com.doogoo.doogoo.academic.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "academic_notices",
        indexes = {
                @Index(name = "idx_academic_start", columnList = "startAt"),
                @Index(name = "idx_academic_grade", columnList = "gradeId")
        }
)
public class AcademicNotice {

    @Id
    @Column
    private String noticeId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String gradeId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    protected AcademicNotice() {}
}
