package com.edstem.tdbvazhipadu.order.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
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

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class DailyLimitIntegrationTest {

    private final MockMvc mockMvc;

    @Test
    void withFourOfFiveBookedAnOrderForTwoIsRejectedAndAnOrderForOneSucceeds() throws Exception {
        long pooja =
                createOffering("{\"type\":\"UDAYASTHAMANA_POOJA\",\"price\":500.00,\"active\":true,\"dailyLimit\":5}");
        LocalDate date = LocalDate.now().plusDays(60);
        placeOrder(pooja, 4, date).andExpect(status().isCreated());

        placeOrder(pooja, 2, date)
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Only 1 UDAYASTHAMANA_POOJA left for " + date));
        placeOrder(pooja, 1, date).andExpect(status().isCreated());
        placeOrder(pooja, 1, date).andExpect(status().isUnprocessableContent());
    }

    @Test
    void theLimitIsCountedPerDate() throws Exception {
        long pooja =
                createOffering("{\"type\":\"UDAYASTHAMANA_POOJA\",\"price\":500.00,\"active\":true,\"dailyLimit\":1}");
        LocalDate date = LocalDate.now().plusDays(61);

        placeOrder(pooja, 1, date).andExpect(status().isCreated());
        placeOrder(pooja, 1, date.plusDays(1)).andExpect(status().isCreated());
    }

    @Test
    void offeringsWithoutALimitAcceptAnyNumberOfOrders() throws Exception {
        long archana = createOffering("{\"type\":\"ARCHANA\",\"price\":50.00,\"active\":true}");
        LocalDate date = LocalDate.now().plusDays(62);

        for (int i = 0; i < 3; i++) {
            placeOrder(archana, 100, date).andExpect(status().isCreated());
        }
    }

    @Test
    void aDailyLimitBelowOneIsRejected() throws Exception {
        mockMvc.perform(post("/api/offerings")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"type\":\"UDAYASTHAMANA_POOJA\",\"price\":500.00,\"active\":true,\"dailyLimit\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'dailyLimit')]").exists());
    }

    private long createOffering(String json) throws Exception {
        String response = mockMvc.perform(post("/api/offerings")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private ResultActions placeOrder(long offeringId, int quantity, LocalDate date) throws Exception {
        return mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"devoteeName\":\"Lakshmi\",\"phoneNumber\":\"9876543210\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":[{\"offeringId\":%d,\"quantity\":%d}]}"
                                .formatted(date, offeringId, quantity)));
    }
}
