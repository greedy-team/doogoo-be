package com.doogoo.doogoo.dodream.application;

import com.doogoo.doogoo.catalog.domain.Keyword;
import com.doogoo.doogoo.dodream.api.dto.DoDreamNoticesResponse;
import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import com.doogoo.doogoo.dodream.infrastructure.DoDreamNoticeRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DoDreamNoticeQueryService {

    private final DoDreamNoticeRepository doDreamNoticeRepository;

    public DoDreamNoticeQueryService(DoDreamNoticeRepository doDreamNoticeRepository) {
        this.doDreamNoticeRepository = doDreamNoticeRepository;
    }

    public DoDreamNoticesResponse getNotices() {
        List<DoDreamNoticesResponse.NoticeItem> items = doDreamNoticeRepository.findAll().stream()
                .map(this::toItem)
                .collect(Collectors.toList());
        return new DoDreamNoticesResponse(items);
    }

    private DoDreamNoticesResponse.NoticeItem toItem(DoDreamNotice n) {
        List<String> keywordIds = n.getKeywords() == null ? List.of() : n.getKeywords().stream().map(Keyword::id).collect(Collectors.toList());
        return new DoDreamNoticesResponse.NoticeItem(
                n.getNoticeId(),
                n.getTitle(),
                n.getDepartmentId(),
                n.getDepartmentName(),
                n.getApplicationStartAt(),
                n.getApplicationEndAt(),
                n.getOperatingStartAt(),
                n.getOperatingEndAt(),
                n.getLocation(),
                n.getDescription(),
                keywordIds,
                n.getDetailUrl()
        );
    }
}
