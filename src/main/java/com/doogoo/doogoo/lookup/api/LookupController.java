package com.doogoo.doogoo.lookup.api;

import com.doogoo.doogoo.lookup.application.LookupQueryService;
import com.doogoo.doogoo.lookup.application.LookupQueryService.IdName;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lookup", description = "학년·학과·키워드 목록")
@RestController
@RequestMapping("/api")
public class LookupController {

    private final LookupQueryService lookupQueryService;


    public LookupController(LookupQueryService lookupQueryService) {
        this.lookupQueryService = lookupQueryService;
    }

    @Operation(summary = "학년 목록")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/grades")
    public GradesResponse grades() {
        return new GradesResponse(lookupQueryService.getGrades());
    }

    @Operation(summary = "학과 목록")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/departments")
    public DepartmentsResponse departments() {
        return new DepartmentsResponse(lookupQueryService.getDepartments());
    }

    @Operation(summary = "키워드 목록")
    @ApiResponse(responseCode = "200", description = "성공")
    @GetMapping("/keywords")
    public KeywordsResponse keywords() {
        return new KeywordsResponse(lookupQueryService.getKeywords());
    }

    @Schema(description = "학년 목록")
    public record GradesResponse(List<IdName> grades) {}
    @Schema(description = "학과 목록")
    public record DepartmentsResponse(List<IdName> departments) {}
    @Schema(description = "키워드 목록")
    public record KeywordsResponse(List<IdName> keywords) {}
}
