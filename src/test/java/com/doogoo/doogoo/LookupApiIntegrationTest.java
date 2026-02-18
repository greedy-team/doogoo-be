package com.doogoo.doogoo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static com.doogoo.doogoo.TestFixtures.performGet;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class LookupApiIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Nested
    @DisplayName("GET /api/grades")
    class Grades {

        @Test
        @DisplayName("200 반환, grades 리스트 필드 존재, null이 아님")
        void returns_200_with_grades_list() throws Exception {
            performGet(mockMvc, "/api/grades")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.grades").exists())
                    .andExpect(jsonPath("$.grades").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/departments")
    class Departments {

        @Test
        @DisplayName("200 반환, departments 리스트 필드 존재, null이 아님")
        void returns_200_with_departments_list() throws Exception {
            performGet(mockMvc, "/api/departments")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.departments").exists())
                    .andExpect(jsonPath("$.departments").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/keywords")
    class Keywords {

        @Test
        @DisplayName("200 반환, keywords 리스트 필드 존재, null이 아님")
        void returns_200_with_keywords_list() throws Exception {
            performGet(mockMvc, "/api/keywords")
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.keywords").exists())
                    .andExpect(jsonPath("$.keywords").isArray());
        }
    }
}
