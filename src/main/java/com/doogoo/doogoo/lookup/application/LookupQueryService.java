package com.doogoo.doogoo.lookup.application;

import com.doogoo.doogoo.lookup.domain.College;
import com.doogoo.doogoo.lookup.domain.Department;
import com.doogoo.doogoo.lookup.domain.Grade;
import com.doogoo.doogoo.lookup.domain.Keyword;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class LookupQueryService {

    public List<IdName> getGrades() {
        return Arrays.stream(Grade.values()).map(g -> new IdName(g.id(), g.displayName())).collect(Collectors.toList());
    }

    public List<CollegeWithDepartments> getColleges() {
        return Arrays.stream(College.values())
                .map(c -> new CollegeWithDepartments(
                        c.id(),
                        c.displayName(),
                        Arrays.stream(Department.values())
                                .filter(d -> d.college() == c)
                                .map(d -> new DepartmentItem(d.id(), d.displayName(), d.tags(), d.contractBranch()))
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());
    }

    public List<IdName> getDepartments() {
        return Arrays.stream(Department.values()).map(d -> new IdName(d.id(), d.displayName())).collect(Collectors.toList());
    }

    public List<KeywordItem> getKeywords() {
        return Arrays.stream(Keyword.values())
                .map(k -> new KeywordItem(k.id(), k.displayName(), k.description(), k.icon()))
                .collect(Collectors.toList());
    }

    @Schema(description = "id·name 쌍")
    public record IdName(
            @Schema(description = "ID", example = "1") String id,
            @Schema(description = "표시명", example = "1학년") String name) {}

    @Schema(description = "학과 한 건 (tags·contractBranch 선택)")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DepartmentItem(
            @Schema(description = "학과 ID", example = "dept-cse") String id,
            @Schema(description = "학과명", example = "컴퓨터공학과") String name,
            @Schema(description = "태그 (ADVANCED, CONTRACT 등)", example = "[\"ADVANCED\"]") List<String> tags,
            @Schema(description = "계약학과 군종", example = "육군") String contractBranch) {}

    @Schema(description = "키워드 한 건")
    public record KeywordItem(
            @Schema(description = "키워드 ID", example = "competition") String id,
            @Schema(description = "키워드명", example = "학술/연구") String name,
            @Schema(description = "설명", example = "경진대회, 공모전, 학술행사") String description,
            @Schema(description = "아이콘 식별자", example = "competition") String icon) {}

    @Schema(description = "학부·소속 학과 목록")
    public record CollegeWithDepartments(
            @Schema(description = "학부 ID", example = "col-ai") String id,
            @Schema(description = "학부명", example = "인공지능융합대학") String name,
            @Schema(description = "소속 학과 목록") List<DepartmentItem> departments) {}
}
