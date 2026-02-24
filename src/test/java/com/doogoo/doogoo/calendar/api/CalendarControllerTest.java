package com.doogoo.doogoo.calendar.api;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CalendarControllerTest {

    private static final String ACADEMIC_ICS_BODY =
            "{\"selectedGradeId\":1}";

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    @DisplayName("token 형식 오류 시 400")
    void invalid_token_format_returns_400() {
        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(baseUrl() + "/cal/ab.ics", String.class));
        assertEquals(400, e.getStatusCode().value());
    }

    @Test
    @DisplayName("존재하지 않는 token 시 404")
    void token_not_found_returns_404() {
        HttpClientErrorException e = assertThrows(HttpClientErrorException.class,
                () -> restTemplate.getForEntity(baseUrl() + "/cal/NonexistentToken123.ics", String.class));
        assertEquals(404, e.getStatusCode().value());
    }

    @Test
    @DisplayName("정상 호출 시 200, text/calendar, body에 BEGIN:VCALENDAR·BEGIN:VEVENT 포함")
    void valid_token_returns_200_with_calendar_body() {
        String token = issueToken();
        ResponseEntity<String> res = restTemplate.getForEntity(baseUrl() + "/cal/" + token + ".ics", String.class);
        assertEquals(200, res.getStatusCode().value());
        assertTrue(res.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE).contains("text/calendar"));
        String body = res.getBody();
        assertTrue(body != null && body.contains("BEGIN:VCALENDAR") && body.contains("BEGIN:VEVENT"));
    }

    @Test
    @DisplayName("download=true 시 Content-Disposition 헤더 존재")
    void download_true_sets_content_disposition() {
        String token = issueToken();
        ResponseEntity<String> res = restTemplate.exchange(
                baseUrl() + "/cal/" + token + ".ics?download=true",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class);
        assertEquals(200, res.getStatusCode().value());
        assertTrue(res.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("attachment"));
    }

    private String issueToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> res = restTemplate.exchange(
                baseUrl() + "/api/academic/ics",
                HttpMethod.POST,
                new HttpEntity<>(ACADEMIC_ICS_BODY, headers),
                String.class);
        return JsonPath.parse(res.getBody()).read("$.token", String.class);
    }
}
