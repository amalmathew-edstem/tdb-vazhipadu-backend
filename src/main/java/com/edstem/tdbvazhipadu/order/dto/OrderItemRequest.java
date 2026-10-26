package com.edstem.tdbvazhipadu.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(@NotNull Long offeringId, @NotNull @Min(1) Integer quantity) {}
