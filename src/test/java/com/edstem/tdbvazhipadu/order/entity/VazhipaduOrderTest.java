package com.edstem.tdbvazhipadu.order.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edstem.tdbvazhipadu.order.exception.OrderNotCancellableException;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VazhipaduOrderTest {

    @Test
    void cancellingABookedOrderMarksItCancelled() {
        VazhipaduOrder order = newOrder();

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void aCompletedOrderCannotBeCancelled() {
        VazhipaduOrder order = newOrder();
        ReflectionTestUtils.setField(order, "status", OrderStatus.COMPLETED);

        assertThatThrownBy(order::cancel)
                .isInstanceOf(OrderNotCancellableException.class)
                .hasMessageContaining("COMPLETED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void anAlreadyCancelledOrderCannotBeCancelledAgain() {
        VazhipaduOrder order = newOrder();
        order.cancel();

        assertThatThrownBy(order::cancel).isInstanceOf(OrderNotCancellableException.class);
    }

    private static VazhipaduOrder newOrder() {
        return new VazhipaduOrder(
                "Lakshmi",
                "9876543210",
                Nakshatra.ROHINI,
                LocalDate.of(2026, 11, 2),
                Instant.parse("2026-10-26T06:00:00Z"));
    }
}
