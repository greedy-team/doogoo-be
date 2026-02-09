package com.doogoo.doogoo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 서버 랜덤 포트 기동 + HTTP로 엔드포인트 호출하는 진짜 통합 테스트.
 * (Spring Boot 컨텍스트 + 실제 웹 계층 + 네트워크 레벨)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("통합: 유효한 token으로 ICS 피드 요청 시 200과 VCALENDAR 본문 반환")
    void ics_정상_통합_테스트() {
        String token = "Abcdef";
        String url = "http://localhost:" + port + "/ics/feed?token=" + token + "&download=false";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String body = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(body).contains("BEGIN:VCALENDAR");
        assertThat(body).contains("BEGIN:VEVENT");
        assertThat(body).contains("END:VCALENDAR");
    }

    @Test
    @DisplayName("통합: download=true 시 Content-Disposition 헤더 포함")
    void ics_download_헤더_통합_테스트() {
        String token = "Abcdef";
        String url = "http://localhost:" + port + "/ics/feed?token=" + token + "&download=true";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .contains("attachment")
                .contains("schedule.ics");
    }

    @Test
    @DisplayName("통합: 잘못된 token 형식 시 400")
    void ics_token_형식_오류_통합_테스트() {
        String url = "http://localhost:" + port + "/ics/feed?token=Invalid&download=false";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("통합: 존재하지 않는 token 시 404")
    void ics_token_없음_통합_테스트() {
        String url = "http://localhost:" + port + "/ics/feed?token=NotExist&download=false";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
