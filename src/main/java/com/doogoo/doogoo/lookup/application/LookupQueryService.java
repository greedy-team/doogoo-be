package com.doogoo.doogoo.lookup.application;

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

    public List<IdName> getDepartments() {
        return Arrays.stream(Department.values()).map(d -> new IdName(d.id(), d.displayName())).collect(Collectors.toList());
    }

    public List<IdName> getKeywords() {
        return Arrays.stream(Keyword.values()).map(k -> new IdName(k.id(), k.displayName())).collect(Collectors.toList());
    }

    public record IdName(String id, String name) {}
}
