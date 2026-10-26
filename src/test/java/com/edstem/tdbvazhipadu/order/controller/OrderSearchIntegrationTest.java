package com.edstem.tdbvazhipadu.order.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class OrderSearchIntegrationTest {

    private final MockMvc mockMvc;

    private long offeringId;

    @BeforeEach
    void createOffering() throws Exception {
        String response = mockMvc.perform(post("/api/offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ARCHANA\",\"price\":50.00,\"active\":true}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        offeringId = ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void searchingByPhoneNumberReturnsOnlyThatDevoteesOrders() throws Exception {
        LocalDate date = LocalDate.now().plusDays(30);
        long first = placeOrder("9000000001", date);
        long second = placeOrder("9000000001", date);
        placeOrder("9000000002", date);

        String response = mockMvc.perform(get("/api/orders").param("phoneNumber", "9000000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<String> phones = JsonPath.read(response, "$.content[*].phoneNumber");
        assertThat(phones).containsOnly("9000000001");
        assertThat(idsOf(response)).containsExactly(second, first);
    }

    @Test
    void pageTwoContinuesFromPageOneWithNoRepeatsOrGaps() throws Exception {
        LocalDate date = LocalDate.now().plusDays(31);
        List<Long> placed = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            placed.add(placeOrder("9000000003", date));
        }

        List<Long> pageOne = searchPage("9000000003", 0);
        List<Long> pageTwo = searchPage("9000000003", 1);
        List<Long> pageThree = searchPage("9000000003", 2);

        assertThat(pageOne).hasSize(2);
        assertThat(pageTwo).hasSize(2).doesNotContainAnyElementsOf(pageOne);
        List<Long> allPages = new ArrayList<>(pageOne);
        allPages.addAll(pageTwo);
        allPages.addAll(pageThree);
        assertThat(allPages)
                .containsExactlyElementsOf(
                        placed.stream().sorted(Comparator.reverseOrder()).toList());
    }

    @Test
    void searchFiltersByOfferingDateAndStatus() throws Exception {
        LocalDate date = LocalDate.now().plusDays(32);
        long onDate = placeOrder("9000000004", date);
        placeOrder("9000000004", date.plusDays(1));

        String byDate = mockMvc.perform(
                        get("/api/orders").param("phoneNumber", "9000000004").param("offeringDate", date.toString()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(idsOf(byDate)).containsExactly(onDate);

        mockMvc.perform(get("/api/orders").param("phoneNumber", "9000000004").param("status", "BOOKED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(2));
        mockMvc.perform(get("/api/orders").param("phoneNumber", "9000000004").param("status", "CANCELLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(0));
    }

    @Test
    void invalidPhoneNumberIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders").param("phoneNumber", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber')]").exists());
    }

    @Test
    void unknownStatusIsRejected() throws Exception {
        mockMvc.perform(get("/api/orders").param("status", "LOST"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[?(@.field == 'status')]").exists());
    }

    private List<Long> searchPage(String phoneNumber, int page) throws Exception {
        String response = mockMvc.perform(get("/api/orders")
                        .param("phoneNumber", phoneNumber)
                        .param("page", String.valueOf(page))
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return idsOf(response);
    }

    private long placeOrder(String phoneNumber, LocalDate offeringDate) throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                "{\"devoteeName\":\"Lakshmi\",\"phoneNumber\":\"%s\",\"nakshatra\":\"ROHINI\",\"offeringDate\":\"%s\",\"items\":[{\"offeringId\":%d,\"quantity\":1}]}"
                                        .formatted(phoneNumber, offeringDate, offeringId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("BOOKED"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private static List<Long> idsOf(String response) {
        List<Number> ids = JsonPath.read(response, "$.content[*].id");
        return ids.stream().map(Number::longValue).toList();
    }
}
