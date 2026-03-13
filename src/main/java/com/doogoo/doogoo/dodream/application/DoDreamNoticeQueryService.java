package com.doogoo.doogoo.dodream.application;

import com.doogoo.doogoo.dodream.api.dto.DoDreamNoticesResponse;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoDreamNoticeQueryService {

    private final EventRepository eventRepository;

    public DoDreamNoticeQueryService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public DoDreamNoticesResponse getNotices() {
        List<DoDreamNoticesResponse.NoticeItem> items = eventRepository.findByStatus(EventStatus.OPEN).stream()
                .map(this::toItem)
                .toList();
        return new DoDreamNoticesResponse(items);
    }

    private DoDreamNoticesResponse.NoticeItem toItem(Event event) {
        return new DoDreamNoticesResponse.NoticeItem(
                "dodream-" + event.getDodreamId(),
                event.getTitle(),
                event.getDepartmentId(),
                event.getDepartment(),
                event.getApplyStart(),
                event.getApplyEnd(),
                event.getOperateStart(),
                event.getOperateEnd(),
                event.getLocation(),
                event.getDescription(),
                event.getDescriptionSummary(),
                event.getMileage(),
                event.getKeywordIds(),
                event.getDodreamUrl()
        );
    }
}
