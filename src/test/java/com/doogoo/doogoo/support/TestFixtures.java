package com.doogoo.doogoo.support;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public final class TestFixtures {

    public static final String VALID_TOKEN = "Abcdef12";
    public static final String ACADEMIC_ICS_REQUEST_JSON = "{\"selectedDepartmentId\":null,\"selectedGradeId\":1,\"alarmEnabled\":false,\"alarmMinutesBefore\":null}";
    public static final String DODREAM_ICS_REQUEST_JSON = "{\"selectedDepartmentId\":null,\"selectedMinorDepartmentId\":null,\"selectedKeywordId\":[],\"alarmEnabled\":false,\"alarmMinutesBefore\":null}";

    private TestFixtures() {
    }

    public static ResultActions performGet(MockMvc mvc, String url) throws Exception {
        return mvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions performPostJson(MockMvc mvc, String url, String jsonBody) throws Exception {
        return mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonBody));
    }
}
