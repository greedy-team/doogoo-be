package com.doogoo.doogoo.global.api;

import com.doogoo.doogoo.calendar.application.IcsService;
import com.doogoo.doogoo.crawl.CrawlScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Admin", description = "관리자 기능")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final CrawlScheduler crawlScheduler;
    private final IcsService icsService;

    public AdminController(CrawlScheduler crawlScheduler, IcsService icsService) {
        this.crawlScheduler = crawlScheduler;
        this.icsService = icsService;
    }

    @Operation(summary = "학사 일정 크롤링 (수동 실행)", description = "특정 연도의 학사 일정을 크롤링합니다.")
    @PostMapping("/crawl/academic/{year}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void crawlAcademicScheduleForYear(@PathVariable int year) {
        crawlScheduler.crawlAcademicScheduleForYear(year);
    }

    @Operation(summary = "현재 연도 학사 일정 크롤링", description = "현재 연도의 학사 일정을 크롤링합니다.")
    @PostMapping("/crawl/academic/current")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void crawlCurrentYear() {
        crawlScheduler.crawlAcademicScheduleForYear(LocalDate.now().getYear());
    }

    @Operation(summary = "현재·이전 연도 학사 일정 크롤링", description = "현재 연도와 이전 연도의 학사 일정을 크롤링합니다.")
    @PostMapping("/crawl/academic/current-and-previous")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void crawlCurrentAndPreviousYear() {
        crawlScheduler.crawlAcademicSchedule();
    }

    @Operation(summary = "두드림 정기 크롤링 (수동 실행)", description = "두드림 공지를 즉시 크롤링합니다.")
    @PostMapping("/crawl/dodream")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void crawlDodream() {
        crawlScheduler.regularCrawl();
    }

    @Operation(summary = "공지 캐시 초기화", description = "두드림 및 학사 공지 캐시를 즉시 비웁니다.")
    @PostMapping("/cache/clear")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearAllCaches() {
        icsService.invalidateAcademicDataByUpdate();
        icsService.invalidateDoDreamDataByUpdate();
    }
}

