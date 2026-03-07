package com.doogoo.doogoo.global.config;

import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.crawl.CrawlScheduler;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Profile("prod")
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AcademicScheduleInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AcademicScheduleInitializer.class);

    private final AcademicScheduleRepository academicScheduleRepository;
    private final CrawlScheduler crawlScheduler;

    public AcademicScheduleInitializer(
            AcademicScheduleRepository academicScheduleRepository,
            CrawlScheduler crawlScheduler) {
        this.academicScheduleRepository = academicScheduleRepository;
        this.crawlScheduler = crawlScheduler;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (academicScheduleRepository.count() > 0) {
            return;
        }
        int year = LocalDate.now().getYear();
        log.info("학사일정 데이터 없음 - 초기 크롤링 실행: year={}, {}", year, year - 1);
        try {
            crawlScheduler.crawlAcademicScheduleForYear(year);
            crawlScheduler.crawlAcademicScheduleForYear(year - 1);
        } catch (Exception e) {
            log.error("[{}] {}: year={}", ErrorCode.ACADEMIC_CRAWL_FAILED.getCode(), ErrorCode.ACADEMIC_CRAWL_FAILED.getMessage(), year, e);
        }
    }
}
