package com.doogoo.doogoo.event;

import com.doogoo.doogoo.classify.AiClassifyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventSyncService {

    private static final Logger log = LoggerFactory.getLogger(EventSyncService.class);

    private final EventRepository eventRepository;

    public EventSyncService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public Event saveNew(EventDto dto, AiClassifyResult aiResult) {
        Event event = Event.createNew(
                dto.dodreamId(), dto.title(), dto.department(),
                dto.applyStart(), dto.applyEnd(),
                dto.operateStart(), dto.operateEnd(),
                dto.description(), dto.thumbnailUrl(), dto.dodreamUrl()
        );
        if (aiResult != null) {
            event.applyAiResult(aiResult.category(), aiResult.location(), aiResult.target(), aiResult.mileage());
        }
        Event saved = eventRepository.save(event);
        log.info("새 공지 저장: dodreamId={}, title={}", dto.dodreamId(), dto.title());
        return saved;
    }

    @Transactional
    public boolean tryUpdateFromList(EventDto dto) {
        return eventRepository.findByDodreamId(dto.dodreamId())
                .map(event -> {
                    if (event.updateFromList(dto.title(), dto.department(), dto.applyStart(), dto.applyEnd(), dto.thumbnailUrl())) {
                        eventRepository.save(event);
                        log.info("공지 업데이트: dodreamId={}", dto.dodreamId());
                    }
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void enrichWithDetail(EventDto detailDto, AiClassifyResult aiResult) {
        eventRepository.findByDodreamId(detailDto.dodreamId()).ifPresent(event -> {
            event.updateDetail(detailDto.description(), detailDto.operateStart(), detailDto.operateEnd());
            if (aiResult != null) {
                event.applyAiResult(aiResult.category(), aiResult.location(), aiResult.target(), aiResult.mileage());
            }
            eventRepository.save(event);
            log.info("공지 상세정보 보강: dodreamId={}", detailDto.dodreamId());
        });
    }

    @Transactional
    public void markClosedBatch(List<Long> dodreamIds) {
        if (dodreamIds.isEmpty()) return;
        int updated = eventRepository.markClosedByDodreamIds(dodreamIds);
        log.info("공지 일괄 마감 처리: {}건 요청, {}건 변경", dodreamIds.size(), updated);
    }

    @Transactional
    public int closeExpiredEvents() {
        int updated = eventRepository.closeExpiredEvents(LocalDateTime.now());
        log.info("만료 공지 일괄 마감 처리: {}건", updated);
        return updated;
    }

    public List<Event> findOpenEvents() {
        return eventRepository.findByStatus(EventStatus.OPEN);
    }
}
