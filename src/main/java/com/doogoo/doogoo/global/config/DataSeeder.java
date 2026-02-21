package com.doogoo.doogoo.global.config;

import com.doogoo.doogoo.academic.domain.AcademicNotice;
import com.doogoo.doogoo.academic.infrastructure.AcademicNoticeRepository;
import com.doogoo.doogoo.catalog.domain.Grade;
import com.doogoo.doogoo.catalog.domain.Keyword;
import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import com.doogoo.doogoo.dodream.infrastructure.DoDreamNoticeRepository;
import java.time.LocalDateTime;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSeeder implements ApplicationRunner {

    private final AcademicNoticeRepository academicNoticeRepository;
    private final DoDreamNoticeRepository doDreamNoticeRepository;

    public DataSeeder(AcademicNoticeRepository academicNoticeRepository, DoDreamNoticeRepository doDreamNoticeRepository) {
        this.academicNoticeRepository = academicNoticeRepository;
        this.doDreamNoticeRepository = doDreamNoticeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (academicNoticeRepository.count() == 0) {
            seedAcademic();
        }
        if (doDreamNoticeRepository.count() == 0) {
            seedDoDream();
        }
    }

    private void seedAcademic() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        academicNoticeRepository.save(new AcademicNotice(
                "seed-academic-1",
                "1학년 등록금 납부 안내",
                Grade.FIRST,
                base.withHour(9).withMinute(0).withSecond(0).withNano(0),
                base.withHour(17).withMinute(0).withSecond(0).withNano(0)
        ));
        academicNoticeRepository.save(new AcademicNotice(
                "seed-academic-2",
                "2학년 장학금 신청 공지",
                Grade.SECOND,
                base.plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0)
        ));
    }

    private void seedDoDream() {
        LocalDateTime base = LocalDateTime.now().plusDays(1);
        doDreamNoticeRepository.save(new DoDreamNotice(
                "seed-dodream-1",
                "학술제 대회 참가 신청",
                "컴퓨터공학과",
                base.withHour(9).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(7).withHour(18).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(11).withHour(17).withMinute(0).withSecond(0).withNano(0),
                Set.of(Keyword.COMPETITION),
                "https://example.com/dodream/1"
        ));
        doDreamNoticeRepository.save(new DoDreamNotice(
                "seed-dodream-2",
                "취창업 특강 안내",
                "경영학과",
                base.plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(9).withHour(18).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(3).withHour(14).withMinute(0).withSecond(0).withNano(0),
                base.plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0),
                Set.of(Keyword.CAREER),
                "https://example.com/dodream/2"
        ));
    }
}
