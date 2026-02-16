package com.doogoo.doogoo.lookup.api;

import com.doogoo.doogoo.lookup.application.LookupQueryService;
import com.doogoo.doogoo.lookup.application.LookupQueryService.IdName;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class LookupController {

    private final LookupQueryService lookupQueryService;

    public LookupController(LookupQueryService lookupQueryService) {
        this.lookupQueryService = lookupQueryService;
    }

    @GetMapping("/grades")
    public GradesResponse grades() {
        return new GradesResponse(lookupQueryService.getGrades());
    }

    @GetMapping("/departments")
    public DepartmentsResponse departments() {
        return new DepartmentsResponse(lookupQueryService.getDepartments());
    }

    @GetMapping("/keywords")
    public KeywordsResponse keywords() {
        return new KeywordsResponse(lookupQueryService.getKeywords());
    }

    public record GradesResponse(List<IdName> grades) {}
    public record DepartmentsResponse(List<IdName> departments) {}
    public record KeywordsResponse(List<IdName> keywords) {}
}
