package com.doogoo.doogoo.dodream.infrastructure;

import com.doogoo.doogoo.dodream.domain.DoDreamNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoDreamNoticeRepository extends JpaRepository<DoDreamNotice, String> {
}
