package com.edstem.tdbvazhipadu.order.dto;

import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long offeringId, OfferingType offeringType, int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}
