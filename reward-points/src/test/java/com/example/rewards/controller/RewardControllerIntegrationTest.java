package com.example.rewards.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RewardControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;

    /** Verifies the endpoint returns monthly totals for seeded customer data. */
    @Test
    void returnsRewardSummaries() throws Exception {
        mockMvc.perform(get("/api/rewards").param("startDate", "2026-01-01").param("endDate", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].customerId").value(1001))
                .andExpect(jsonPath("$[0].monthlyRewards[0].month").value("2026-01"))
                .andExpect(jsonPath("$[0].monthlyRewards[0].points").value(115))
                .andExpect(jsonPath("$[0].monthlyRewards[1].points").value(250))
                .andExpect(jsonPath("$[0].totalPoints").value(365));
    }

    /** Verifies a reversed date range returns HTTP 400. */
    @Test
    void rejectsReversedDateRange() throws Exception {
        mockMvc.perform(get("/api/rewards").param("startDate", "2026-03-31").param("endDate", "2026-01-01"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }

    /** Verifies malformed dates return HTTP 400. */
    @Test
    void rejectsMalformedDate() throws Exception {
        mockMvc.perform(get("/api/rewards").param("startDate", "bad-date").param("endDate", "2026-03-31"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").exists());
    }

    /** Verifies required query parameters cannot be omitted. */
    @Test
    void rejectsMissingDateParameter() throws Exception {
        mockMvc.perform(get("/api/rewards").param("startDate", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }
}
