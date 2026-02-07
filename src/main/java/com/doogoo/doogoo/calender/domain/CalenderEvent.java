package com.doogoo.doogoo.calender.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import org.springframework.data.annotation.Id;

import java.time.ZonedDateTime;

public class CalenderEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private final ZonedDateTime startDate;

    @Column(nullable = false)
    private final ZonedDateTime endDate;

    @Column(nullable = false)
    private final String summary;

    @Column(nullable = false)
    private final String description;

    private final Long location;

    private final String alarm;

    @Column(nullable = false)
    private final String dtstamp;

    public CalenderEvent(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            String summary,
            String description,
            Long location,
            String alarm,
            String dtstamp
    ) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.summary = summary;
        this.description = description;
        this.location = location;
        this.alarm = alarm;
        this.dtstamp = dtstamp;
    }



}
