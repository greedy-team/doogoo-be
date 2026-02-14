package com.doogoo.doogoo.event;

import com.doogoo.doogoo.classify.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
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

    private LocalDateTime applyStart;
    private LocalDateTime applyEnd;
    private LocalDateTime operateStart;
    private LocalDateTime operateEnd;

    private String location;
    private String target;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String thumbnailUrl;
    private String dodreamUrl;

    @Enumerated(EnumType.STRING)
    private CategoryType category;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private Integer mileage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Event() {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
                .location(location)
                .target(target)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .dodreamUrl(dodreamUrl)
                .category(category)
                .status(status)
                .mileage(mileage)
                .build();
    }

    public static Event createNew(
            Long dodreamId, String title, String department,
            LocalDateTime applyStart, LocalDateTime applyEnd,
            LocalDateTime operateStart, LocalDateTime operateEnd,
            String description, String thumbnailUrl, String dodreamUrl
    ) {
        Event event = new Event();
        event.dodreamId = dodreamId;
        event.title = title;
        event.department = department;
        event.applyStart = applyStart;
        event.applyEnd = applyEnd;
        event.operateStart = operateStart;
        event.operateEnd = operateEnd;
        event.description = description;
        event.thumbnailUrl = thumbnailUrl;
        event.dodreamUrl = dodreamUrl;
        event.status = EventStatus.OPEN;
        return event;
    }

    public boolean updateFromList(
            String title, String department,
            LocalDateTime applyStart, LocalDateTime applyEnd,
            String thumbnailUrl
    ) {
        boolean changed = false;
        if (!Objects.equals(this.title, title)) { this.title = title; changed = true; }
        if (!Objects.equals(this.department, department)) { this.department = department; changed = true; }
        if (!Objects.equals(this.applyStart, applyStart)) { this.applyStart = applyStart; changed = true; }
        if (!Objects.equals(this.applyEnd, applyEnd)) { this.applyEnd = applyEnd; changed = true; }
        if (!Objects.equals(this.thumbnailUrl, thumbnailUrl)) { this.thumbnailUrl = thumbnailUrl; changed = true; }
        return changed;
    }

    public void updateDetail(String description, LocalDateTime operateStart, LocalDateTime operateEnd) {
        this.description = description;
        if (operateStart != null) this.operateStart = operateStart;
        if (operateEnd != null) this.operateEnd = operateEnd;
    }

    public void applyAiResult(CategoryType category, String location, String target, Integer mileage) {
        this.category = category;
        this.location = location;
        this.target = target;
        this.mileage = mileage;
    }

    public void markClosed() {
        this.status = EventStatus.CLOSED;
    }

    public Long getDodreamId() { return dodreamId; }
    public EventStatus getStatus() { return status; }
}
