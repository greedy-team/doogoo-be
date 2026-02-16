package com.doogoo.doogoo.academic.application;

import com.doogoo.doogoo.academic.api.dto.AcademicNoticesResponse;
import com.doogoo.doogoo.academic.domain.AcademicNotice;
import com.doogoo.doogoo.academic.infrastructure.AcademicNoticeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AcademicNoticeQueryService {

    private final AcademicNoticeRepository academicNoticeRepository;

    public AcademicNoticeQueryService(AcademicNoticeRepository academicNoticeRepository) {
        this.academicNoticeRepository = academicNoticeRepository;
    }

    public AcademicNoticesResponse getNotices() {
        List<AcademicNoticesResponse.NoticeItem> items = academicNoticeRepository.findAll().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        return new AcademicNoticesResponse(items);
    }

    private AcademicNoticesResponse.NoticeItem toItem(AcademicNotice n) {
        String gradeId = n.getGrade() != null ? n.getGrade().id() : null;
        return new AcademicNoticesResponse.NoticeItem(
                n.getNoticeId(),
                n.getTitle(),
                gradeId,
                n.getStartAt(),
                n.getEndAt()
        );
    }
}
