package com.edstem.tdbvazhipadu.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long offeringId, String offeringName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
