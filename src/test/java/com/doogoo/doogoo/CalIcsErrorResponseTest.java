package com.doogoo.doogoo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class CalIcsErrorResponseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("GET /cal/{token}.ics 에러 응답")
    class CalIcsError {

        @Test
        @DisplayName("token 형식 오류 시 400, application/json, ErrorResponse 필드 존재")
        void invalid_token_format_returns_400_with_json_error_response() throws Exception {
            mockMvc.perform(get("/cal/invalid%21.ics")
                            .accept(MediaType.APPLICATION_JSON, MediaType.parseMediaType("text/calendar")))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.code").value("INVALID_TOKEN_FORMAT"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("존재하지 않는 token(형식 유효) 시 404, ErrorResponse 구조 검증")
        void token_not_found_returns_404_with_error_response() throws Exception {
            mockMvc.perform(get("/cal/Nonexist1.ics")
                            .accept(MediaType.APPLICATION_JSON, MediaType.parseMediaType("text/calendar")))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").exists())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.code").exists())
                    .andExpect(jsonPath("$.code").value("TOKEN_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }
}
