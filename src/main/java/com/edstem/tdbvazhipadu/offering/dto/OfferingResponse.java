package com.edstem.tdbvazhipadu.offering.dto;

import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import java.math.BigDecimal;

public record OfferingResponse(Long id, OfferingType type, BigDecimal price, boolean active) {}
