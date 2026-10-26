package com.edstem.tdbvazhipadu.order.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class OrderCancellationIntegrationTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    private final MockMvc mockMvc;

    @Test
    void staffCanCancelABookedOrder() throws Exception {
        long offering = createOffering("{\"type\":\"ARCHANA\",\"price\":50.00,\"active\":true}");
        long order = idOf(placeOrder(offering, LocalDate.now().plusDays(70)).andExpect(status().isCreated()));

        mockMvc.perform(post("/api/orders/{id}/cancel", order).with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(get("/api/orders/{id}", order))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancellingAnOrderTwiceIsRejected() throws Exception {
        long offering = createOffering("{\"type\":\"ARCHANA\",\"price\":50.00,\"active\":true}");
        long order = idOf(placeOrder(offering, LocalDate.now().plusDays(71)).andExpect(status().isCreated()));
        mockMvc.perform(post("/api/orders/{id}/cancel", order).with(staff())).andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{id}/cancel", order).with(staff()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Order " + order + " is CANCELLED and cannot be cancelled"));
    }

    @Test
    void cancellingAnUnknownOrderReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", 999_999).with(staff()))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void cancellingWithoutAStaffTokenIsRejected() throws Exception {
        mockMvc.perform(post("/api/orders/{id}/cancel", 1))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", PROBLEM_JSON));
    }

    @Test
    void aCancelledOrderFreesItsDailyLimit() throws Exception {
        long pooja =
                createOffering("{\"type\":\"UDAYASTHAMANA_POOJA\",\"price\":500.00,\"active\":true,\"dailyLimit\":1}");
        LocalDate date = LocalDate.now().plusDays(72);
        long first = idOf(placeOrder(pooja, date).andExpect(status().isCreated()));
        placeOrder(pooja, date).andExpect(status().isUnprocessableContent());

        mockMvc.perform(post("/api/orders/{id}/cancel", first).with(staff())).andExpect(status().isOk());

        placeOrder(pooja, date).andExpect(status().isCreated());
    }

    private long createOffering(String json) throws Exception {
        String response = mockMvc.perform(post("/api/offerings")
                        .with(staff())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions placeOrder(long offeringId, LocalDate date) throws Exception {
        return mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"devoteeName\":\"Lakshmi\",\"phoneNumber\":\"9876543210\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":[{\"offeringId\":%d,\"quantity\":1}]}"
                                .formatted(date, offeringId)));
    }

    private static long idOf(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }

    private static RequestPostProcessor staff() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"));
    }
}
