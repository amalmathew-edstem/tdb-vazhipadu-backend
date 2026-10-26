package com.edstem.tdbvazhipadu.offering.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record OfferingRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin(value = "0.00", inclusive = false) @Digits(integer = 8, fraction = 2) BigDecimal price,
        @NotNull Boolean active) {}
