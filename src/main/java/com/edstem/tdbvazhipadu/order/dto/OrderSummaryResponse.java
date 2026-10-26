package com.edstem.tdbvazhipadu.order.dto;

import com.edstem.tdbvazhipadu.order.entity.Nakshatra;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record OrderSummaryResponse(
        Long id,
        String devoteeName,
        String phoneNumber,
        Nakshatra nakshatra,
        LocalDate offeringDate,
        OrderStatus status,
        BigDecimal totalAmount,
        Instant createdAt) {}
