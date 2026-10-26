package com.edstem.tdbvazhipadu.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.when;

import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import com.edstem.tdbvazhipadu.offering.repository.OfferingRepository;
import com.edstem.tdbvazhipadu.order.dto.CreateOrderRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderItemRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderResponse;
import com.edstem.tdbvazhipadu.order.entity.Nakshatra;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import com.edstem.tdbvazhipadu.order.exception.InactiveOfferingException;
import com.edstem.tdbvazhipadu.order.exception.UnknownOfferingException;
import com.edstem.tdbvazhipadu.order.mapper.OrderMapper;
import com.edstem.tdbvazhipadu.order.repository.OrderRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-26T06:00:00Z");

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OfferingRepository offeringRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository, offeringRepository, new OrderMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void createCalculatesTotalFromOfferingPricesAndQuantities() {
        Offering pushpanjali = offering(1L, OfferingType.PUSHPANJALI, "30.00", true);
        Offering archana = offering(2L, OfferingType.ARCHANA, "50.00", true);
        when(offeringRepository.findAllById(anyIterable())).thenReturn(List.of(pushpanjali, archana));
        when(orderRepository.save(any(VazhipaduOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response =
                orderService.create(request(List.of(new OrderItemRequest(1L, 2), new OrderItemRequest(2L, 1))));

        assertThat(response.totalAmount()).isEqualByComparingTo("110.00");
        assertThat(response.items())
                .extracting(item -> item.lineTotal().toPlainString())
                .containsExactly("60.00", "50.00");
        assertThat(response.createdAt()).isEqualTo(NOW);
        assertThat(response.status()).isEqualTo(OrderStatus.BOOKED);
    }

    @Test
    void createRejectsInactiveOffering() {
        Offering archana = offering(2L, OfferingType.ARCHANA, "50.00", false);
        when(offeringRepository.findAllById(anyIterable())).thenReturn(List.of(archana));

        assertThatThrownBy(() -> orderService.create(request(List.of(new OrderItemRequest(2L, 1)))))
                .isInstanceOf(InactiveOfferingException.class)
                .hasMessageContaining("ARCHANA");
    }

    @Test
    void createRejectsUnknownOffering() {
        when(offeringRepository.findAllById(anyIterable())).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.create(request(List.of(new OrderItemRequest(99L, 1)))))
                .isInstanceOf(UnknownOfferingException.class)
                .hasMessageContaining("99");
    }

    private static CreateOrderRequest request(List<OrderItemRequest> items) {
        return new CreateOrderRequest("Lakshmi", "9876543210", Nakshatra.ROHINI, LocalDate.of(2026, 11, 2), items);
    }

    private static Offering offering(Long id, OfferingType type, String price, boolean active) {
        Offering offering = new Offering(type, new BigDecimal(price), active, null);
        ReflectionTestUtils.setField(offering, "id", id);
        return offering;
    }
}
