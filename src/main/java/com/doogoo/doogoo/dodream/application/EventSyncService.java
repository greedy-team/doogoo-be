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
    public Event saveNew(EventDto dto, AiClassifyResult aiResult, String summary) {
        Event event = Event.createNew(
                dto.dodreamId(), dto.title(), dto.department(),
                dto.applyStart(), dto.applyEnd(),
                dto.operateStart(), dto.operateEnd(),
                dto.description(), dto.location(), dto.mileage(), dto.dodreamUrl()
        );
        String departmentId = resolveDepartmentId(dto.title());
        if (aiResult != null) {
            event.applyAiResult(aiResult.keywords(), departmentId);
        } else {
            event.applyAiResult(List.of("k_7"), departmentId);
        }
        event.applyDescriptionSummary(summary);
        Event saved = eventRepository.save(event);
        icsService.invalidateDoDreamDataByUpdate();
        return saved;
    }

    @Transactional
    public boolean tryUpdateFromList(EventDto dto) {
        return eventRepository.findByDodreamId(dto.dodreamId())
                .map(event -> {
                    if (event.updateFromList(dto.title(), dto.department(), dto.applyStart(), dto.applyEnd())) {
                        eventRepository.save(event);
                        icsService.invalidateDoDreamDataByUpdate();
                    }
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void enrichWithDetail(EventDto detailDto, AiClassifyResult aiResult, String summary) {
        eventRepository.findByDodreamId(detailDto.dodreamId()).ifPresent(event -> {
            event.updateDetail(detailDto.description(), detailDto.location(), detailDto.mileage(), detailDto.operateStart(), detailDto.operateEnd());
            if (aiResult != null) {
                event.applyAiResult(aiResult.keywords(), event.getDepartmentId());
            }
            event.applyDescriptionSummary(summary);
            eventRepository.save(event);
            icsService.invalidateDoDreamDataByUpdate();
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
    public int deleteExpiredEvents() {
        int deleted = eventRepository.deleteExpiredEvents(LocalDateTime.now());
        if (deleted > 0) icsService.invalidateDoDreamDataByUpdate();
        log.info("만료 공지 일괄 삭제 처리: {}건", deleted);
        return deleted;
    }

    public List<Event> findOpenEvents() {
        return eventRepository.findByStatus(EventStatus.OPEN);
    }

    private String resolveDepartmentId(String title) {
        if (title != null && !title.isBlank()) {
            return Arrays.stream(Department.values())
                    .filter(d -> title.contains(d.displayName()))
                    .map(Department::id)
                    .findFirst()
                    .orElse("all");
        }
        return "all";
    }
}
