package com.bolsillo.ahorro.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class GoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void postValidGoalReturns201() throws Exception {
        String name = unique("Meta alta");
        mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"targetAmount\":500000}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.targetAmount").value(500000))
                .andExpect(jsonPath("$.currentAmount").value(0))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getListReturns200AndSeed() throws Exception {
        mockMvc.perform(get("/api/goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name=='Viaje a Cartagena')]").exists())
                .andExpect(jsonPath("$[?(@.name=='Fondo emergencia')]").exists());
    }

    @Test
    void validContributionReturns200AndUpdatesCurrent() throws Exception {
        String id = createGoal(unique("Meta abono"), "1000");

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":250}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.currentAmount").value(250))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void zeroContributionReturns400() throws Exception {
        String id = createGoal(unique("Meta cero"), "100");

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiExceptionHandler.VALIDATION_ERROR));
    }

    @Test
    void negativeContributionReturns400() throws Exception {
        String id = createGoal(unique("Meta negativo"), "100");

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":-10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value(ApiExceptionHandler.VALIDATION_ERROR));
    }

    @Test
    void contributionExceedingRemainingReturns422() throws Exception {
        String id = createGoal(unique("Meta restante"), "100");
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
    void contributionOnCompletedGoalReturns422() throws Exception {
        String id = createGoal(unique("Meta cerrada"), "100");
        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":1}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.code").value("GOAL_ALREADY_COMPLETED"));
    }

    @Test
    void contributionThatReachesTargetReturns200Completed() throws Exception {
        String id = createGoal(unique("Meta cien"), "100");

        mockMvc.perform(post("/api/goals/" + id + "/contributions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentAmount").value(100))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.progressPercent").value(100));
    }

    @Test
    void concurrentContributionsKeepCurrentWithinTarget() throws Exception {
        String id = createGoal(unique("Meta concurrente"), "100");
        try (ExecutorService pool = Executors.newFixedThreadPool(2)) {
            Callable<Integer> contribute60 = () -> mockMvc.perform(
                            post("/api/goals/" + id + "/contributions")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"amount\":60}"))
                    .andReturn()
                    .getResponse()
                    .getStatus();
            Future<Integer> first = pool.submit(contribute60);
            Future<Integer> second = pool.submit(contribute60);
            List<Integer> statuses = List.of(first.get(), second.get());

            assertThat(statuses).contains(200);
            assertThat(statuses).allMatch(status -> status == 200 || status == 409 || status == 422);

            String body = mockMvc.perform(get("/api/goals/" + id))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
            BigDecimal current = JsonPath.parse(body).read("$.currentAmount", BigDecimal.class);
            assertThat(current).isLessThanOrEqualTo(new BigDecimal("100"));
        }
    }

    private String createGoal(String name, String target) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"targetAmount\":" + target + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(created.getResponse().getContentAsString(), "$.id");
    }

    private static String unique(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }
}
