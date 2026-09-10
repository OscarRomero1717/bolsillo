package com.bolsillo.ahorro.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void zeroContributionAmountReturns400Not500() throws Exception {
        String id = createGoal("Meta validacion", "100");

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiExceptionHandler.VALIDATION_ERROR))
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void missingGoalReturns404() throws Exception {
        mockMvc.perform(get("/api/goals/c0a80100-0000-4000-8000-000000000099"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("GOAL_NOT_FOUND"));
    }

    @Test
    void contributionExceedingRemainingReturns422() throws Exception {
        String id = createGoal("Meta 422", "100");
        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":80}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":30}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("CONTRIBUTION_EXCEEDS_REMAINING"));
    }

    @Test
    void optimisticLockMapsTo409() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        ProblemDetail problem = handler.handleConflict(new OptimisticLockingFailureException("stale"));

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getProperties()).containsEntry("code", ApiExceptionHandler.CONCURRENT_MODIFICATION);
    }

    private String createGoal(String name, String target) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"targetAmount\":" + target + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    }
}
