package com.doogoo.doogoo.calendar.application;

import com.doogoo.doogoo.academic.domain.AcademicSchedule;
import com.doogoo.doogoo.academic.infrastructure.AcademicScheduleRepository;
import com.doogoo.doogoo.dodream.domain.Event;
import com.doogoo.doogoo.dodream.domain.EventStatus;
import com.doogoo.doogoo.dodream.infrastructure.EventRepository;
import com.doogoo.doogoo.subscription.application.SubscriptionReader;
import com.doogoo.doogoo.subscription.domain.SourceType;
import com.doogoo.doogoo.subscription.domain.Subscription;
import com.doogoo.doogoo.subscription.infrastructure.SubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IcsCacheTest {

    @Mock
    private AcademicScheduleRepository academicScheduleRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private SubscriptionReader subscriptionReader;

    private Cache<String, String> icsCache;
    private Cache<String, Subscription> tokenCache;
    private Cache<String, List<AcademicSchedule>> academicNoticesCache;
    private Cache<String, List<Event>> doDreamNoticesCache;

    private IcsService icsService;

    @BeforeEach
    void setUp() {
        icsCache = Caffeine.newBuilder().maximumSize(1000).recordStats().build();
        tokenCache = Caffeine.newBuilder().maximumSize(1000).recordStats().build();
        academicNoticesCache = Caffeine.newBuilder().maximumSize(10).recordStats().build();
        doDreamNoticesCache = Caffeine.newBuilder().maximumSize(10).recordStats().build();

        icsService = new IcsService(
                academicScheduleRepository,
                eventRepository,
                subscriptionRepository,
                subscriptionReader,
                new ObjectMapper(),
                icsCache,
                tokenCache,
                academicNoticesCache,
                doDreamNoticesCache,
                new Semaphore(10)
        );
    }

    // ─── 헬퍼 ─────────────────────────────────────────────────────────────────

    private Subscription academicSub(String token, String filterHash) {
        return new Subscription(token, SourceType.ACADEMIC, "{\"selectedGradeId\":1}", false, null, filterHash);
    }

    private Subscription doDreamSub(String token, String filterHash) {
        return new Subscription(token, SourceType.DODREAM, "{}", false, null, filterHash);
    }

    private AcademicSchedule academicSchedule() {
        return AcademicSchedule.create(2026, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 6, 30), "봄학기", "1");
    }

    private Event doDreamEvent() {
        return Event.createNew(
                1L, "테스트 이벤트", "컴퓨터공학과",
                LocalDateTime.now(), LocalDateTime.now().plusDays(7),
                LocalDateTime.now(), LocalDateTime.now().plusDays(14),
                "테스트 설명", "서울", "1000", "https://example.com"
        );
    }

    // ─── tokenCache 테스트 ───────────────────────────────────────────────────

    @Test
    @DisplayName("tokenCache: 동일 토큰 두 번 조회 시 DB는 한 번만 호출됨")
    void tokenCache_hit_on_second_call() {
        String token = "ValidToken123456";
        when(subscriptionReader.getByToken(token)).thenReturn(academicSub(token, "hash001"));

        icsService.getSubscriptionByToken(token);
        icsService.getSubscriptionByToken(token);

        verify(subscriptionReader, times(1)).getByToken(token);
        assertThat(tokenCache.stats().hitCount()).isEqualTo(1);
        assertThat(tokenCache.stats().missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("tokenCache: 서로 다른 토큰은 각각 DB를 조회함")
    void tokenCache_miss_on_different_tokens() {
        String token1 = "ValidTokenAAAA01";
        String token2 = "ValidTokenBBBB02";
        when(subscriptionReader.getByToken(token1)).thenReturn(academicSub(token1, "hashA"));
        when(subscriptionReader.getByToken(token2)).thenReturn(academicSub(token2, "hashB"));

        icsService.getSubscriptionByToken(token1);
        icsService.getSubscriptionByToken(token2);

        verify(subscriptionReader, times(1)).getByToken(token1);
        verify(subscriptionReader, times(1)).getByToken(token2);
        assertThat(tokenCache.stats().missCount()).isEqualTo(2);
    }

    // ─── icsCache 테스트 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("icsCache: 동일 filterHash의 ICS는 두 번째 요청에서 렌더링 없이 반환됨")
    void icsCache_hit_on_same_filter_hash() {
        String token = "ValidToken123456";
        Subscription sub = academicSub(token, "hash001");
        when(subscriptionReader.getByToken(token)).thenReturn(sub);
        when(academicScheduleRepository.findAll()).thenReturn(List.of(academicSchedule()));

        icsService.getIcsByToken(token); // 캐시 미스 → DB 조회 + 렌더링
        icsService.getIcsByToken(token); // 캐시 히트 → 렌더링 없음

        verify(academicScheduleRepository, times(1)).findAll();
        assertThat(icsCache.stats().hitCount()).isEqualTo(1);
        assertThat(icsCache.stats().missCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("icsCache: filterHash가 다르면 각각 렌더링됨")
    void icsCache_miss_on_different_filter_hash() {
        String token1 = "ValidTokenAAAA01";
        String token2 = "ValidTokenBBBB02";
        when(subscriptionReader.getByToken(token1)).thenReturn(academicSub(token1, "hashA"));
        when(subscriptionReader.getByToken(token2)).thenReturn(academicSub(token2, "hashB"));
        when(academicScheduleRepository.findAll()).thenReturn(List.of(academicSchedule()));

        icsService.getIcsByToken(token1);
        icsService.getIcsByToken(token2);

        assertThat(icsCache.stats().missCount()).isEqualTo(2);
        assertThat(icsCache.asMap()).containsKey("ACADEMIC:hashA");
        assertThat(icsCache.asMap()).containsKey("ACADEMIC:hashB");
    }

    // ─── academicNoticesCache 테스트 ─────────────────────────────────────────

    @Test
    @DisplayName("academicNoticesCache: 학사일정은 첫 렌더링에만 DB에서 조회되고 이후 캐시 히트")
    void academicNoticesCache_hit_on_second_render() {
        // 다른 filterHash → icsCache는 미스지만 academicNoticesCache는 히트
        String token1 = "ValidTokenAAAA01";
        String token2 = "ValidTokenBBBB02";
        when(subscriptionReader.getByToken(token1)).thenReturn(academicSub(token1, "hashA"));
        when(subscriptionReader.getByToken(token2)).thenReturn(academicSub(token2, "hashB"));
        when(academicScheduleRepository.findAll()).thenReturn(List.of(academicSchedule()));

        icsService.getIcsByToken(token1); // academicNoticesCache 미스
        icsService.getIcsByToken(token2); // icsCache 미스이지만 academicNoticesCache 히트

        verify(academicScheduleRepository, times(1)).findAll();
        assertThat(academicNoticesCache.stats().hitCount()).isEqualTo(1);
        assertThat(academicNoticesCache.stats().missCount()).isEqualTo(1);
    }

    // ─── doDreamNoticesCache 테스트 ──────────────────────────────────────────

    @Test
    @DisplayName("doDreamNoticesCache: 두드림 이벤트는 첫 렌더링에만 DB에서 조회되고 이후 캐시 히트")
    void doDreamNoticesCache_hit_on_second_render() {
        String token1 = "ValidTokenAAAA01";
        String token2 = "ValidTokenBBBB02";
        when(subscriptionReader.getByToken(token1)).thenReturn(doDreamSub(token1, "hashA"));
        when(subscriptionReader.getByToken(token2)).thenReturn(doDreamSub(token2, "hashB"));
        when(eventRepository.findByStatus(EventStatus.OPEN)).thenReturn(List.of(doDreamEvent()));

        icsService.getIcsByToken(token1); // doDreamNoticesCache 미스
        icsService.getIcsByToken(token2); // icsCache 미스이지만 doDreamNoticesCache 히트

        verify(eventRepository, times(1)).findByStatus(EventStatus.OPEN);
        assertThat(doDreamNoticesCache.stats().hitCount()).isEqualTo(1);
        assertThat(doDreamNoticesCache.stats().missCount()).isEqualTo(1);
    }

    // ─── invalidateAcademicDataByUpdate 테스트 ───────────────────────────────

    @Test
    @DisplayName("invalidateAcademicDataByUpdate: academicNoticesCache와 ACADEMIC ICS 캐시만 삭제됨")
    void invalidateAcademicDataByUpdate_clears_only_academic_caches() {
        academicNoticesCache.put("ACADEMIC", List.of(academicSchedule()));
        icsCache.put("ACADEMIC:hash001", "BEGIN:VCALENDAR...");
        icsCache.put("ACADEMIC:hash002", "BEGIN:VCALENDAR...");
        icsCache.put("DODREAM:hash001", "BEGIN:VCALENDAR..."); // 삭제되면 안 됨

        icsService.invalidateAcademicDataByUpdate();

        assertThat(academicNoticesCache.asMap()).isEmpty();
        assertThat(icsCache.asMap()).doesNotContainKey("ACADEMIC:hash001");
        assertThat(icsCache.asMap()).doesNotContainKey("ACADEMIC:hash002");
        assertThat(icsCache.asMap()).containsKey("DODREAM:hash001");
    }

    @Test
    @DisplayName("invalidateAcademicDataByUpdate: 캐시가 비어있어도 예외 없이 실행됨")
    void invalidateAcademicDataByUpdate_on_empty_cache_no_exception() {
        icsService.invalidateAcademicDataByUpdate(); // 예외 없이 통과해야 함

        assertThat(academicNoticesCache.asMap()).isEmpty();
        assertThat(icsCache.asMap()).isEmpty();
    }

    // ─── invalidateDoDreamDataByUpdate 테스트 ───────────────────────────────

    @Test
    @DisplayName("invalidateDoDreamDataByUpdate: doDreamNoticesCache와 DODREAM ICS 캐시만 삭제됨")
    void invalidateDoDreamDataByUpdate_clears_only_dodream_caches() {
        doDreamNoticesCache.put("DODREAM", List.of(doDreamEvent()));
        icsCache.put("DODREAM:hash001", "BEGIN:VCALENDAR...");
        icsCache.put("DODREAM:hash002", "BEGIN:VCALENDAR...");
        icsCache.put("ACADEMIC:hash001", "BEGIN:VCALENDAR..."); // 삭제되면 안 됨

        icsService.invalidateDoDreamDataByUpdate();

        assertThat(doDreamNoticesCache.asMap()).isEmpty();
        assertThat(icsCache.asMap()).doesNotContainKey("DODREAM:hash001");
        assertThat(icsCache.asMap()).doesNotContainKey("DODREAM:hash002");
        assertThat(icsCache.asMap()).containsKey("ACADEMIC:hash001");
    }

    @Test
    @DisplayName("invalidateDoDreamDataByUpdate: 캐시가 비어있어도 예외 없이 실행됨")
    void invalidateDoDreamDataByUpdate_on_empty_cache_no_exception() {
        icsService.invalidateDoDreamDataByUpdate(); // 예외 없이 통과해야 함

        assertThat(doDreamNoticesCache.asMap()).isEmpty();
        assertThat(icsCache.asMap()).isEmpty();
    }

    // ─── 캐시 독립성 테스트 ───────────────────────────────────────────────────

    @Test
    @DisplayName("ACADEMIC 무효화 후 재조회 시 DB를 다시 호출함")
    void after_academic_invalidate_db_is_called_again() {
        String token = "ValidToken123456";
        Subscription sub = academicSub(token, "hash001");
        when(subscriptionReader.getByToken(token)).thenReturn(sub);
        when(academicScheduleRepository.findAll()).thenReturn(List.of(academicSchedule()));

        icsService.getIcsByToken(token); // 캐시 채움
        icsService.invalidateAcademicDataByUpdate(); // 캐시 비움
        icsService.getIcsByToken(token); // 다시 DB 조회해야 함

        verify(academicScheduleRepository, times(2)).findAll();
    }

    @Test
    @DisplayName("DODREAM 무효화 후 재조회 시 DB를 다시 호출함")
    void after_dodream_invalidate_db_is_called_again() {
        String token = "ValidToken123456";
        Subscription sub = doDreamSub(token, "hash001");
        when(subscriptionReader.getByToken(token)).thenReturn(sub);
        when(eventRepository.findByStatus(EventStatus.OPEN)).thenReturn(List.of(doDreamEvent()));

        icsService.getIcsByToken(token); // 캐시 채움
        icsService.invalidateDoDreamDataByUpdate(); // 캐시 비움
        icsService.getIcsByToken(token); // 다시 DB 조회해야 함

        verify(eventRepository, times(2)).findByStatus(EventStatus.OPEN);
    }
}
