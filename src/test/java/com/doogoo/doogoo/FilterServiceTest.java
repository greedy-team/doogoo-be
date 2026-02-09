package com.doogoo.doogoo;

import com.doogoo.doogoo.domain.Filter;
import com.doogoo.doogoo.domain.Notice;
import com.doogoo.doogoo.service.FilterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FilterServiceTest {

    private FilterService filterService;
    private List<Notice> notices;

    @BeforeEach
    void setUp() {
        filterService = new FilterService();
        notices = List.of(
                new Notice("컴공 장학금 안내", "컴퓨터공학과"),
                new Notice("경영학과 MT 공지", "경영학과"),
                new Notice("전체 등록금 납부 안내", "공통"),
                new Notice("소프트웨어학과 해커톤 참가자 모집", "소프트웨어학과"),
                new Notice("컴퓨터공학과 졸업요건 변경", "컴퓨터공학과")
        );
    }

    @Test
    @DisplayName("학과 필터링이 정확히 동작하는가")
    void 학과_필터링_테스트() {
        // given
        Filter filter = new Filter(List.of("컴퓨터공학과"), List.of());

        // when
        List<Notice> result = filterService.apply(notices, filter);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(n -> n.getDepartment().equals("컴퓨터공학과"));
    }

    @Test
    @DisplayName("키워드 필터링이 정확히 동작하는가")
    void 키워드_필터링_테스트() {
        // given
        Filter filter = new Filter(List.of(), List.of("장학금"));

        // when
        List<Notice> result = filterService.apply(notices, filter);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("장학금");
    }

    @Test
    @DisplayName("학과 + 키워드 조합 필터링")
    void 학과_키워드_조합_필터링_테스트() {
        // given
        Filter filter = new Filter(List.of("컴퓨터공학과"), List.of("장학금"));

        // when
        List<Notice> result = filterService.apply(notices, filter);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).contains("장학금");
        assertThat(result.get(0).getDepartment()).isEqualTo("컴퓨터공학과");
    }

    @Test
    @DisplayName("매칭 결과가 없는 경우 빈 리스트 반환")
    void 매칭_결과_없으면_빈_리스트_반환() {
        // given
        Filter filter = new Filter(List.of("물리학과"), List.of("천문학"));

        // when
        List<Notice> result = filterService.apply(notices, filter);

        // then
        assertThat(result).isEmpty();
    }
}
