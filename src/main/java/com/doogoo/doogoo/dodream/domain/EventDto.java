package com.doogoo.doogoo.dodream.domain;

import java.time.LocalDateTime;
import java.util.List;

public record EventDto(
        Long dodreamId,
        String title,
        String department,
        LocalDateTime applyStart,
        LocalDateTime applyEnd,
        LocalDateTime operateStart,
        LocalDateTime operateEnd,
        String description,
        String location,
        String mileage,
        String dodreamUrl,
        List<String> keywordIds,
        EventStatus status
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
                .description(source.description)
                .location(source.location)
                .mileage(source.mileage)
                .dodreamUrl(source.dodreamUrl)
                .keywordIds(source.keywordIds)
                .status(source.status);
    }

    public static final class Builder {
        private Long dodreamId;
        private String title;
        private String department;
        private LocalDateTime applyStart;
        private LocalDateTime applyEnd;
        private LocalDateTime operateStart;
        private LocalDateTime operateEnd;
        private String description;
        private String location;
        private String dodreamUrl;
        private String mileage;
        private List<String> keywordIds;
        private EventStatus status;

        private Builder() {}

        public Builder dodreamId(Long dodreamId) { this.dodreamId = dodreamId; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder department(String department) { this.department = department; return this; }
        public Builder applyStart(LocalDateTime applyStart) { this.applyStart = applyStart; return this; }
        public Builder applyEnd(LocalDateTime applyEnd) { this.applyEnd = applyEnd; return this; }
        public Builder operateStart(LocalDateTime operateStart) { this.operateStart = operateStart; return this; }
        public Builder operateEnd(LocalDateTime operateEnd) { this.operateEnd = operateEnd; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder location(String location) { this.location = location; return this; }
        public Builder mileage(String mileage) { this.mileage = mileage; return this; }
        public Builder dodreamUrl(String dodreamUrl) { this.dodreamUrl = dodreamUrl; return this; }
        public Builder keywordIds(List<String> keywordIds) { this.keywordIds = keywordIds; return this; }
        public Builder status(EventStatus status) { this.status = status; return this; }

        public EventDto build() {
            return new EventDto(
                    dodreamId, title, department,
                    applyStart, applyEnd, operateStart, operateEnd,
                    description, location, mileage, dodreamUrl,
                    keywordIds, status
            );
        }
    }
}
