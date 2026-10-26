package com.edstem.tdbvazhipadu.order.dto;

import com.edstem.tdbvazhipadu.order.entity.Nakshatra;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Long id,
        String devoteeName,
        String phoneNumber,
        Nakshatra nakshatra,
        LocalDate offeringDate,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items) {}
