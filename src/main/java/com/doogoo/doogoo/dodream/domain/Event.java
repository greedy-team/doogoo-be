package com.doogoo.doogoo.dodream.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long dodreamId;

    private String title;
    private String department;
    private String departmentId;
    private String location;

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;
    private LocalDateTime operateStart;
    private LocalDateTime operateEnd;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String dodreamUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_keywords", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "keyword_id")
    private List<String> keywordIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Event() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Event createNew(
            Long dodreamId, String title, String department,
            LocalDateTime applyStart, LocalDateTime applyEnd,
            LocalDateTime operateStart, LocalDateTime operateEnd,
            String description, String location, String dodreamUrl
    ) {
        Event event = new Event();
        event.dodreamId = dodreamId;
        event.title = title;
        event.department = department;
        event.departmentId = "all";
        event.applyStart = applyStart;
        event.applyEnd = applyEnd;
        event.operateStart = operateStart;
        event.operateEnd = operateEnd;
        event.description = description;
        event.location = location;
        event.dodreamUrl = dodreamUrl;
        event.status = EventStatus.OPEN;
        return event;
    }

    public boolean updateFromList(
            String title, String department,
            LocalDateTime applyStart, LocalDateTime applyEnd
    ) {
        boolean changed = false;
        if (!Objects.equals(this.title, title)) { this.title = title; changed = true; }
        if (!Objects.equals(this.department, department)) { this.department = department; changed = true; }
        if (!Objects.equals(this.applyStart, applyStart)) { this.applyStart = applyStart; changed = true; }
        if (!Objects.equals(this.applyEnd, applyEnd)) { this.applyEnd = applyEnd; changed = true; }
        return changed;
    }

    public void updateDetail(String description, String location, LocalDateTime operateStart, LocalDateTime operateEnd) {
        this.description = description;
        if (location != null) this.location = location;
        if (operateStart != null) this.operateStart = operateStart;
        if (operateEnd != null) this.operateEnd = operateEnd;
    }

    public void applyAiResult(List<String> keywords, String departmentId) {
        if (keywords != null) {
            this.keywordIds = new ArrayList<>(keywords);
        }
        if (departmentId != null) {
            this.departmentId = departmentId;
        }
    }

    public void markClosed() {
        this.status = EventStatus.CLOSED;
    }

    public EventDto toDto() {
        return EventDto.builder()
                .dodreamId(dodreamId)
                .title(title)
                .department(department)
                .applyStart(applyStart)
                .applyEnd(applyEnd)
                .operateStart(operateStart)
                .operateEnd(operateEnd)
                .description(description)
                .location(location)
                .dodreamUrl(dodreamUrl)
                .keywordIds(keywordIds)
                .status(status)
                .build();
    }

    public Long getId() { return id; }
    public Long getDodreamId() { return dodreamId; }
    public String getTitle() { return title; }
    public String getDepartment() { return department; }
    public String getDepartmentId() { return departmentId; }
    public String getLocation() { return location; }
    public LocalDateTime getApplyStart() { return applyStart; }
    public LocalDateTime getApplyEnd() { return applyEnd; }
    public LocalDateTime getOperateStart() { return operateStart; }
    public LocalDateTime getOperateEnd() { return operateEnd; }
    public String getDescription() { return description; }
    public String getDodreamUrl() { return dodreamUrl; }
    public List<String> getKeywordIds() { return keywordIds; }
    public EventStatus getStatus() { return status; }
}
