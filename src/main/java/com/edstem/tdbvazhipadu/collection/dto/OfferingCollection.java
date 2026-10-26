package com.edstem.tdbvazhipadu.collection.dto;

import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import java.math.BigDecimal;

public record OfferingCollection(Long offeringId, OfferingType offeringType, Long quantity, BigDecimal amount) {}
