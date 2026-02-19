package com.doogoo.doogoo.academic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AcademicScheduleSyncService {

    private static final Logger log = LoggerFactory.getLogger(AcademicScheduleSyncService.class);

    private final AcademicScheduleRepository repository;

    public AcademicScheduleSyncService(AcademicScheduleRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void replaceByYear(int year, List<AcademicScheduleDto> dtos) {
        repository.deleteAllByYear(year);
        List<AcademicSchedule> entities = dtos.stream()
                .map(dto -> AcademicSchedule.create(
                        dto.year(), dto.department(), dto.startDate(), dto.endDate(), dto.content()))
                .toList();
        repository.saveAll(entities);
        log.info("{}년도 학사일정 {}건 저장 완료", year, entities.size());
    }
}
