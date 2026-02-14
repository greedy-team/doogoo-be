package com.doogoo.doogoo.event;

import com.doogoo.doogoo.classify.CategoryType;

import java.time.LocalDateTime;

public record EventDto(
        Long dodreamId,
        String title,
        String department,
        LocalDateTime applyStart,
        LocalDateTime applyEnd,
        LocalDateTime operateStart,
        LocalDateTime operateEnd,
        String location,
        String target,
        String description,
        String thumbnailUrl,
        String dodreamUrl,
        CategoryType category,
        EventStatus status,
        Integer mileage
) {

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(EventDto source) {
        return new Builder()
                .dodreamId(source.dodreamId)
                .title(source.title)
                .department(source.department)
                .applyStart(source.applyStart)
                .applyEnd(source.applyEnd)
                .operateStart(source.operateStart)
                .operateEnd(source.operateEnd)
                .location(source.location)
                .target(source.target)
                .description(source.description)
                .thumbnailUrl(source.thumbnailUrl)
                .dodreamUrl(source.dodreamUrl)
                .category(source.category)
                .status(source.status)
                .mileage(source.mileage);
    }

    public static final class Builder {
        private Long dodreamId;
        private String title;
        private String department;
        private LocalDateTime applyStart;
        private LocalDateTime applyEnd;
        private LocalDateTime operateStart;
        private LocalDateTime operateEnd;
        private String location;
        private String target;
        private String description;
        private String thumbnailUrl;
        private String dodreamUrl;
        private CategoryType category;
        private EventStatus status;
        private Integer mileage;

        private Builder() {}

        public Builder dodreamId(Long dodreamId) { this.dodreamId = dodreamId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder applyStart(LocalDateTime applyStart) { this.applyStart = applyStart; return this; }
        public Builder applyEnd(LocalDateTime applyEnd) { this.applyEnd = applyEnd; return this; }
        public Builder operateStart(LocalDateTime operateStart) { this.operateStart = operateStart; return this; }
        public Builder operateEnd(LocalDateTime operateEnd) { this.operateEnd = operateEnd; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder target(String target) { this.target = target; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder thumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; return this; }
        public Builder dodreamUrl(String dodreamUrl) { this.dodreamUrl = dodreamUrl; return this; }
        public Builder category(CategoryType category) { this.category = category; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }
        public Builder mileage(Integer mileage) { this.mileage = mileage; return this; }

        public EventDto build() {
            return new EventDto(
                    dodreamId, title, department,
                    applyStart, applyEnd, operateStart, operateEnd,
                    location, target, description,
                    thumbnailUrl, dodreamUrl,
                    category, status, mileage
            );
        }
    }
}
