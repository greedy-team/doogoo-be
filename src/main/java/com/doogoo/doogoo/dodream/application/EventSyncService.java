package com.doogoo.doogoo.dodream.application;

import com.doogoo.doogoo.calendar.application.IcsService;
import com.doogoo.doogoo.classify.AiClassifyResult;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventDto;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
import com.doogoo.doogoo.lookup.domain.Department;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class EventSyncService {

    private static final Logger log = LoggerFactory.getLogger(EventSyncService.class);

    private final EventRepository eventRepository;
    private final IcsService icsService;

    public EventSyncService(EventRepository eventRepository, @Lazy IcsService icsService) {
        this.eventRepository = eventRepository;
        this.icsService = icsService;
    }

    @Transactional
    public Event saveNew(EventDto dto, AiClassifyResult aiResult) {
        Event event = Event.createNew(
                dto.dodreamId(), dto.title(), dto.department(),
                dto.applyStart(), dto.applyEnd(),
                dto.operateStart(), dto.operateEnd(),
                dto.description(), dto.location(), dto.mileage(), dto.dodreamUrl()
        );
        String departmentId = resolveDepartmentId(dto.department());
        if (aiResult != null) {
            event.applyAiResult(aiResult.keywords(), departmentId);
        } else {
            event.applyAiResult(List.of("k_7"), departmentId);
        }
        Event saved = eventRepository.save(event);
        icsService.invalidateDoDreamDataByUpdate();
        log.info("새 공지 저장: dodreamId={}, title={}", dto.dodreamId(), dto.title());
        return saved;
    }

    @Transactional
    public boolean tryUpdateFromList(EventDto dto) {
        return eventRepository.findByDodreamId(dto.dodreamId())
                .map(event -> {
                    if (event.updateFromList(dto.title(), dto.department(), dto.applyStart(), dto.applyEnd())) {
                        eventRepository.save(event);
                        icsService.invalidateDoDreamDataByUpdate();
                        log.info("공지 업데이트: dodreamId={}", dto.dodreamId());
                    }
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void enrichWithDetail(EventDto detailDto, AiClassifyResult aiResult) {
        eventRepository.findByDodreamId(detailDto.dodreamId()).ifPresent(event -> {
            event.updateDetail(detailDto.description(), detailDto.location(), detailDto.mileage(), detailDto.operateStart(), detailDto.operateEnd());
            if (aiResult != null) {
                event.applyAiResult(aiResult.keywords(), event.getDepartmentId());
            }
            eventRepository.save(event);
            icsService.invalidateDoDreamDataByUpdate();
            log.info("공지 상세정보 보강: dodreamId={}", detailDto.dodreamId());
        });
    }

    @Transactional
    public void markClosedBatch(List<Long> dodreamIds) {
        if (dodreamIds.isEmpty()) return;
        int updated = eventRepository.markClosedByDodreamIds(dodreamIds);
        if (updated > 0) icsService.invalidateDoDreamDataByUpdate();
        log.info("공지 일괄 마감 처리: {}건 요청, {}건 변경", dodreamIds.size(), updated);
    }

    @Transactional
    public int closeExpiredEvents() {
        int updated = eventRepository.closeExpiredEvents(LocalDateTime.now());
        if (updated > 0) icsService.invalidateDoDreamDataByUpdate();
        log.info("만료 공지 일괄 마감 처리: {}건", updated);
        return updated;
    }

    public List<Event> findOpenEvents() {
        return eventRepository.findByStatus(EventStatus.OPEN);
    }

    private String resolveDepartmentId(String departmentName) {
        if (departmentName == null || departmentName.isBlank()) return "all";
        return Arrays.stream(Department.values())
                .filter(d -> d.displayName().equals(departmentName))
                .map(Department::id)
                .findFirst()
                .orElse("all");
    }
}
