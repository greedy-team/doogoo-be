package com.doogoo.doogoo.academic.application;

import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import com.doogoo.doogoo.academic.domain.AcademicScheduleDto;
import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import com.doogoo.doogoo.calendar.application.IcsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AcademicScheduleSyncService {

    private static final Logger log = LoggerFactory.getLogger(AcademicScheduleSyncService.class);

    private final AcademicScheduleRepository repository;
    private final IcsService icsService;

    public AcademicScheduleSyncService(AcademicScheduleRepository repository, @Lazy IcsService icsService) {
        this.repository = repository;
        this.icsService = icsService;
    }

    @Transactional
    public void replaceByYear(int year, List<AcademicScheduleDto> dtos) {
        repository.deleteAllByYear(year);
        // DB unique key(year, start_date, content) 기준으로 중복 제거
        // endDate가 달라도 같은 키면 첫 번째 것만 유지
        Map<String, AcademicScheduleDto> deduped = new LinkedHashMap<>();
        for (AcademicScheduleDto dto : dtos) {
            String key = dto.year() + "|" + dto.startDate() + "|" + dto.content();
            deduped.putIfAbsent(key, dto);
        }
        List<AcademicSchedule> entities = deduped.values().stream()
                .map(dto -> AcademicSchedule.create(
                        dto.year(), dto.startDate(), dto.endDate(), dto.content(), dto.gradeId()))
                .toList();
        repository.saveAll(entities);
        icsService.invalidateAcademicDataByUpdate();
        log.info("{}년도 학사일정 {}건 저장 완료 (원본 {}건에서 중복 제거)", year, entities.size(), dtos.size());
    }
}
