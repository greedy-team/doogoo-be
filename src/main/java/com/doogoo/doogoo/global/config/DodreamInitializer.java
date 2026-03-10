package com.doogoo.doogoo.global.config;

import com.doogoo.doogoo.common.error.ErrorCode;
import com.doogoo.doogoo.crawl.CrawlScheduler;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
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
public class DodreamInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DodreamInitializer.class);

    private final EventRepository eventRepository;
    private final CrawlScheduler crawlScheduler;

    public DodreamInitializer(EventRepository eventRepository, CrawlScheduler crawlScheduler) {
        this.eventRepository = eventRepository;
        this.crawlScheduler = crawlScheduler;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (eventRepository.count() > 0) {
            return;
        }
        log.info("두드림 공지 데이터 없음 - 초기 크롤링 실행");
        try {
            crawlScheduler.regularCrawl();
        } catch (Exception e) {
            log.error("[{}] {}: {}", ErrorCode.CRAWL_FAILED.getCode(), ErrorCode.CRAWL_FAILED.getMessage(), e.getMessage(), e);
        }
    }
}
