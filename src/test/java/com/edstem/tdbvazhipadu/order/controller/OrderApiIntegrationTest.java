package com.edstem.tdbvazhipadu.order.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class OrderApiIntegrationTest {

    private static final String PROBLEM_JSON = "application/problem+json";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void orderForTwoPushpanjaliAndOneArchanaTotalsOneHundredTen() throws Exception {
        long pushpanjali = createOffering("PUSHPANJALI", "30.00", true);
        long archana = createOffering("ARCHANA", "50.00", true);

        String created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("[{\"offeringId\":%d,\"quantity\":2},{\"offeringId\":%d,\"quantity\":1}]"
                                .formatted(pushpanjali, archana))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(110.0))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long orderId = ((Number) JsonPath.read(created, "$.id")).longValue();
        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.devoteeName").value("Lakshmi"))
                .andExpect(jsonPath("$.nakshatra").value("ROHINI"))
                .andExpect(jsonPath("$.totalAmount").value(110.0))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].offeringType").value("PUSHPANJALI"));
    }

    @Test
    void orderForInactiveOfferingIsRejected() throws Exception {
        long inactive = createOffering("GANAPATHI_HOMAM", "100.00", false);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("[{\"offeringId\":%d,\"quantity\":1}]".formatted(inactive))))
                .andExpect(status().isUnprocessableContent())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value("Offering GANAPATHI_HOMAM is not active and cannot be ordered"));
    }

    @Test
    void invalidOrderIsRejectedWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"devoteeName\":\"\",\"phoneNumber\":\"abc\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":[]}"
                                        .formatted(LocalDate.now().plusDays(7))))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors[?(@.field == 'devoteeName')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber')]").exists())
                .andExpect(jsonPath("$.errors[?(@.field == 'items')]").exists());
    }

    @Test
    void unknownOrderReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 999_999))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Order 999999 was not found"));
    }

    private long createOffering(String type, String price, boolean active) throws Exception {
        String response = mockMvc.perform(post("/api/offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"%s\",\"price\":%s,\"active\":%s}".formatted(type, price, active)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static String orderJson(String itemsJson) {
        return "{\"devoteeName\":\"Lakshmi\",\"phoneNumber\":\"9876543210\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":%s}"
                .formatted(LocalDate.now().plusDays(7), itemsJson);
    }
}
