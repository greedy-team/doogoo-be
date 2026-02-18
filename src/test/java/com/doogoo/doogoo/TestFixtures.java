package com.doogoo.doogoo;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class TestFixtures {

    public static final String VALID_TOKEN = "Abcdef12";
    public static final String ACADEMIC_ICS_REQUEST_JSON = "{\"selectedDepartmentId\":null,\"selectedGradeId\":1,\"alarmEnabled\":false,\"alarmMinutesBefore\":null}";
    public static final String DODREAM_ICS_REQUEST_JSON = "{\"selectedDepartmentId\":null,\"selectedMinorDepartmentId\":null,\"selectedKeywordId\":[\"k_1\"],\"alarmEnabled\":false,\"alarmMinutesBefore\":null}";

    private TestFixtures() {
    }

    public static ResultActions performGet(MockMvc mockMvc, String path) throws Exception {
        return mockMvc.perform(get(path).accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions performGetIcs(MockMvc mockMvc, String token, boolean download) throws Exception {
        String url = "/cal/" + token + ".ics" + (download ? "?download=true" : "");
        return mockMvc.perform(get(url).accept("text/calendar"));
    }

    public static ResultActions performPostAcademicIcs(MockMvc mockMvc, String body) throws Exception {
        return mockMvc.perform(post("/api/academic/ics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    public static ResultActions performPostDoDreamIcs(MockMvc mockMvc, String body) throws Exception {
        return mockMvc.perform(post("/api/dodream/ics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    public static void expect200(ResultActions actions) throws Exception {
        actions.andExpect(status().isOk());
    }
}
