package com.doogoo.doogoo.academic.infrastructure;

import com.doogoo.doogoo.academic.domain.AcademicNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicNoticeRepository extends JpaRepository<AcademicNotice, String> {
}
