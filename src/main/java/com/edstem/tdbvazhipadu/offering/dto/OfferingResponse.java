package com.edstem.tdbvazhipadu.offering.dto;

import java.math.BigDecimal;

public record OfferingResponse(Long id, String name, BigDecimal price, boolean active) {}
