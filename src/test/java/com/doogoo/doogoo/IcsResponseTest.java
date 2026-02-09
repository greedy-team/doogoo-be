package com.doogoo.doogoo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IcsResponseTest {

    @Test
    @DisplayName("유효한 token으로 요청하면 ics 데이터를 반환한다 (200)")
    void ics_정상_반환_테스트() {
        // given
        String token = "Abcdef";
        boolean download = false;

        // when
        ResponseEntity<String> response = icsController.getFeed(token, download);
        String body = response.getBody();

        // then
        assertThat(body).contains("BEGIN:VCALENDAR");
        assertThat(body).contains("BEGIN:VEVENT");
        assertThat(body).contains("DTSTART");
        assertThat(body).contains("DTEND");
        assertThat(body).contains("SUMMARY:");
        assertThat(body).contains("DESCRIPTION:");
        assertThat(body).contains("DTSTAMP:");
        assertThat(body).contains("END:VEVENT");
        assertThat(body).contains("END:VCALENDAR");
    }

    @Test
    @DisplayName("download=true이면 Content-Disposition 헤더가 포함된다")
    void ics_download_true_헤더_테스트() {
        // given
        String token = "Abcdef";
        boolean download = true;

        // when
        ResponseEntity<String> response = icsController.getFeed(token, download);

        // then
        assertThat(response.getHeaders()).containsEntry("Content-Disposition", "attachment; filename=\"schedule.ics\"");
    }

    @Test
    @DisplayName("token 형식이 올바르지 않으면 400을 반환한다")
    void token_형식_오류_400_테스트() {
        // given
        String invalidToken = "Invalid";
        boolean download = false;

        // when & then
        assertThatThrownBy(() -> icsController.getFeed(invalidToken, download)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("존재하지 않는 token이면 404를 반환한다")
    void token_없음_404_테스트() {
        // given
        String notExistToken = "NotExist";
        boolean download = false;

        // when & then
        assertThatThrownBy(() -> icsController.getFeed(notExistToken, download)).isInstanceOf(NotFoundException.class);
    }
}
