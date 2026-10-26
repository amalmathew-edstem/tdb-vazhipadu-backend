package com.edstem.tdbvazhipadu.collection.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.tdbvazhipadu.collection.dto.DailyCollectionResponse;
import com.edstem.tdbvazhipadu.collection.dto.OfferingCollection;
import com.edstem.tdbvazhipadu.collection.service.CollectionService;
import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import com.edstem.tdbvazhipadu.offering.repository.OfferingRepository;
import com.edstem.tdbvazhipadu.order.entity.Nakshatra;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import com.edstem.tdbvazhipadu.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class DailyCollectionIntegrationTest {

    private final MockMvc mockMvc;
    private final OfferingRepository offeringRepository;
    private final OrderRepository orderRepository;
    private final CollectionService collectionService;

    @Test
    void anOrderPlacedAtQuarterPastMidnightIstCountsOnThatDate() throws Exception {
        Offering archana = offering(OfferingType.ARCHANA, "50.00");
        placedAtIst(LocalDateTime.of(2025, 3, 10, 0, 15), archana, 2);
        placedAtIst(LocalDateTime.of(2025, 3, 9, 23, 59), archana, 1);

        mockMvc.perform(get("/api/collections/daily")
                        .param("date", "2025-03-10")
                        .with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timeZone").value("Asia/Kolkata"))
                .andExpect(jsonPath("$.offerings.length()").value(1))
                .andExpect(jsonPath("$.offerings[0].quantity").value(2))
                .andExpect(jsonPath("$.totalAmount").value(100.0));
        mockMvc.perform(get("/api/collections/daily")
                        .param("date", "2025-03-09")
                        .with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings[0].quantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(50.0));
    }

    @Test
    void aCancelledOrderIsNotCountedInTheReport() throws Exception {
        Offering pushpanjali = offering(OfferingType.PUSHPANJALI, "30.00");
        placedAtIst(LocalDateTime.of(2025, 3, 11, 9, 0), pushpanjali, 1);
        VazhipaduOrder cancelled = placedAtIst(LocalDateTime.of(2025, 3, 11, 10, 0), pushpanjali, 5);

        mockMvc.perform(post("/api/orders/{id}/cancel", cancelled.getId()).with(staff()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/collections/daily")
                        .param("date", "2025-03-11")
                        .with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings[0].quantity").value(1))
                .andExpect(jsonPath("$.totalAmount").value(30.0));
    }

    @Test
    void theReportTotalEqualsTheSumOfOfferingTotalsToThePaisa() throws Exception {
        Offering pushpanjali = offering(OfferingType.PUSHPANJALI, "30.50");
        Offering mala = offering(OfferingType.MALA, "0.10");
        Offering archana = offering(OfferingType.ARCHANA, "99.99");
        placedAtIst(LocalDateTime.of(2025, 3, 12, 7, 0), pushpanjali, 3);
        placedAtIst(LocalDateTime.of(2025, 3, 12, 8, 0), pushpanjali, 1);
        placedAtIst(LocalDateTime.of(2025, 3, 12, 9, 0), mala, 7);
        placedAtIst(LocalDateTime.of(2025, 3, 12, 10, 0), archana, 2);

        DailyCollectionResponse report = collectionService.daily(LocalDate.of(2025, 3, 12));

        BigDecimal sumOfOfferings =
                report.offerings().stream().map(OfferingCollection::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(report.totalAmount()).isEqualByComparingTo(sumOfOfferings);
        assertThat(report.totalAmount()).isEqualTo(new BigDecimal("322.68"));
        assertThat(report.offerings())
                .extracting(collection -> collection.amount().toPlainString())
                .containsExactlyInAnyOrder("122.00", "0.70", "199.98");
        String json = mockMvc.perform(get("/api/collections/daily")
                        .param("date", "2025-03-12")
                        .with(staff()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(json).contains("\"totalAmount\":322.68");
    }

    @Test
    void aDayWithNoOrdersReportsZero() throws Exception {
        mockMvc.perform(get("/api/collections/daily")
                        .param("date", "2025-01-01")
                        .with(staff()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.offerings.length()").value(0))
                .andExpect(jsonPath("$.totalAmount").value(0.0));
    }

    @Test
    void theReportIsForStaffOnly() throws Exception {
        mockMvc.perform(get("/api/collections/daily").param("date", "2025-03-10"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Content-Type", "application/problem+json"));
    }

    private Offering offering(OfferingType type, String price) {
        return offeringRepository.save(new Offering(type, new BigDecimal(price), true, null));
    }

    private VazhipaduOrder placedAtIst(LocalDateTime istTime, Offering offering, int quantity) {
        VazhipaduOrder order = new VazhipaduOrder(
                "Lakshmi",
                "9876543210",
                Nakshatra.ROHINI,
                istTime.toLocalDate(),
                istTime.atZone(CollectionService.TEMPLE_ZONE).toInstant());
        order.addItem(offering, quantity);
        return orderRepository.save(order);
    }

    private static RequestPostProcessor staff() {
        return jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"));
    }
}
