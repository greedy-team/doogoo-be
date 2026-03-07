package com.doogoo.doogoo.global.config;

import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile({"dev", "test"})
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSeeder implements ApplicationRunner {

    private final AcademicScheduleRepository academicScheduleRepository;
    private final EventRepository eventRepository;

    public DataSeeder(AcademicScheduleRepository academicScheduleRepository, EventRepository eventRepository) {
        this.academicScheduleRepository = academicScheduleRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (academicScheduleRepository.count() == 0) {
            seedAcademicSchedules();
        }
        if (eventRepository.count() == 0) {
            seedEvents();
        }
    }

    private void seedAcademicSchedules() {
        LocalDate base = LocalDate.now();
        int year = base.getYear();
        // 수강신청: 오늘~+4일(영업일 기준 5일), 4→3→2→1→전체 순 배정됨
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(1), base.plusDays(1), "수강신청", "4"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(2), base.plusDays(2), "수강신청", "3"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(3), base.plusDays(3), "수강신청", "2"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(4), base.plusDays(4), "수강신청", "1"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(5), base.plusDays(5), "수강신청", null));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(10), base.plusDays(10), "입학식", "1"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(20), base.plusDays(20), "제80회 학위수여식", "4"));
        academicScheduleRepository.save(AcademicSchedule.create(
                year, "학부", base.plusDays(30), base.plusDays(40), "1학기 수업 기간", null));
    }

    private void seedEvents() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);

        Event event1 = Event.createNew(
                1L,
                "학술제 대회 참가 신청",
                "컴퓨터공학과",
                base.withHour(9).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(7).withHour(18).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(11).withHour(17).withMinute(0).withSecond(0).withNano(0),
                "AI 해커톤...",
                "대양AI센터 B101호",
                "https://do.sejong.ac.kr/ko/program/all/view/1"
        );
        event1.applyAiResult(List.of("k_1"), "dept-cse");
        eventRepository.save(event1);

        Event event2 = Event.createNew(
                2L,
                "취창업 특강 안내",
                "경영학부",
                base.plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(9).withHour(18).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0),
                null,
                null,
                "https://do.sejong.ac.kr/ko/program/all/view/2"
        );
        event2.applyAiResult(List.of("k_2"), "all");
        eventRepository.save(event2);
    }
}
