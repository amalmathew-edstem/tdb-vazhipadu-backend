package com.edstem.tdbvazhipadu.offering.dto;

import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OfferingRequest(
        @NotNull OfferingType type,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull Boolean active) {}
