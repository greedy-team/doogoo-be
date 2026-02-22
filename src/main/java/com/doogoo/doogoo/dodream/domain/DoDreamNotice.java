package com.doogoo.doogoo.dodream.domain;

import com.doogoo.doogoo.catalog.domain.Keyword;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dodream_notices")
public class DoDreamNotice {

    @Id
    private String noticeId;

    @Column(nullable = false)
    private String title;

    private String departmentName;

    private LocalDateTime applicationStartAt;
    private LocalDateTime applicationEndAt;

    @Column(nullable = false)
    private LocalDateTime operatingStartAt;

    private LocalDateTime operatingEndAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @Column(name = "keyword", nullable = false)
    private Set<Keyword> keywords = new HashSet<>();

    @Column(nullable = false)
    private String detailUrl;

    protected DoDreamNotice() {}

    public DoDreamNotice(String noticeId, String title, String departmentName,
                         LocalDateTime applicationStartAt, LocalDateTime applicationEndAt,
                         LocalDateTime operatingStartAt, LocalDateTime operatingEndAt,
                         Set<Keyword> keywords, String detailUrl) {
        this.noticeId = noticeId;
        this.title = title;
        this.departmentName = departmentName;
        this.applicationStartAt = applicationStartAt;
        this.applicationEndAt = applicationEndAt;
        this.operatingStartAt = operatingStartAt;
        this.operatingEndAt = operatingEndAt;
        if (keywords != null) {
            this.keywords.addAll(keywords);
        }
        this.detailUrl = detailUrl;
    }

    public String getNoticeId() { return noticeId; }
    public String getTitle() { return title; }
    public String getDepartmentName() { return departmentName; }
    public java.time.LocalDateTime getApplicationStartAt() { return applicationStartAt; }
    public java.time.LocalDateTime getApplicationEndAt() { return applicationEndAt; }
    public java.time.LocalDateTime getOperatingStartAt() { return operatingStartAt; }
    public java.time.LocalDateTime getOperatingEndAt() { return operatingEndAt; }
    public java.util.Set<Keyword> getKeywords() { return keywords; }
    public String getDetailUrl() { return detailUrl; }
}
