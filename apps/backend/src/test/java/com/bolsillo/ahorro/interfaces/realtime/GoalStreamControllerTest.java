package com.bolsillo.ahorro.interfaces.realtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class GoalStreamControllerTest {

    private static final String VIAJE_ID = "c0a80100-0000-4000-8000-000000000001";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void streamIsEventStreamNotAGoalId() throws Exception {
        mockMvc.perform(get("/api/goals/stream").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andExpect(status().isOk());
    }

    @Test
    void contributionIsPushedToOpenStream() throws Exception {
        MvcResult stream = mockMvc.perform(get("/api/goals/stream").accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(post("/api/goals/" + VIAJE_ID + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1000}"))
                .andExpect(status().isOk());

        String body = stream.getResponse().getContentAsString();
        assertThat(body).contains("event:goal-updated");
        assertThat(body).contains("Viaje a Cartagena");
        assertThat(body).contains(VIAJE_ID);
    }
}
