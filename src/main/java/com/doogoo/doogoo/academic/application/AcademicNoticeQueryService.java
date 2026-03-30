package com.doogoo.doogoo.academic.application;

import com.doogoo.doogoo.academic.api.dto.AcademicNoticesResponse;
import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AcademicNoticeQueryService {

    private final AcademicScheduleRepository academicScheduleRepository;

    public AcademicNoticeQueryService(AcademicScheduleRepository academicScheduleRepository) {
        this.academicScheduleRepository = academicScheduleRepository;
    }

    public AcademicNoticesResponse getNotices() {
        List<AcademicNoticesResponse.NoticeItem> items = academicScheduleRepository.findAll().stream()
                .map(this::toItem)
                .toList();
        return new AcademicNoticesResponse(items);
    }

    private AcademicNoticesResponse.NoticeItem toItem(AcademicSchedule n) {
        return new AcademicNoticesResponse.NoticeItem(
                String.valueOf(n.getId()),
                n.getContent(),
                n.getStartDate().atStartOfDay(),
                n.getEndDate().atTime(23, 59),
                n.getGradeId() == null ? AcademicSchedule.ALL_GRADE_ID : n.getGradeId()
        );
    }
}
