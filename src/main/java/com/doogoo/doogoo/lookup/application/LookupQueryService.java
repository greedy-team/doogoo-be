package com.doogoo.doogoo.lookup.application;

import com.doogoo.doogoo.lookup.domain.College;
import com.doogoo.doogoo.lookup.domain.Department;
import com.doogoo.doogoo.lookup.domain.Grade;
import com.doogoo.doogoo.lookup.domain.Keyword;
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

    public record IdName(String id, String name) {}

    public record DepartmentItem(String id, String name, List<String> tags, String contractBranch) {}

    public record KeywordItem(String id, String name, String description, String icon) {}

    public record CollegeWithDepartments(String id, String name, List<DepartmentItem> departments) {}
}
