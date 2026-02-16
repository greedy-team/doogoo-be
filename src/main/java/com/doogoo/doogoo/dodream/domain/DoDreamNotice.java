package com.doogoo.doogoo.dodream.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
    @CollectionTable(name = "dodream_notice_keywords", joinColumns = @JoinColumn(name = "noticeId"))
    @Column(name = "keywordId", nullable = false)
    private Set<String> keywordIds;

    @Column(nullable = false)
    private String detailUrl;

    protected DoDreamNotice() {}
}
