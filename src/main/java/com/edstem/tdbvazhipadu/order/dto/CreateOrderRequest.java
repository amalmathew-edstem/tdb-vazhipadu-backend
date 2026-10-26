package com.edstem.tdbvazhipadu.order.dto;

import com.edstem.tdbvazhipadu.order.entity.Nakshatra;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Size(max = 100) String devoteeName,
        @NotBlank
                @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "must be 10 to 14 digits, optionally starting with +")
                String phoneNumber,
        @NotNull Nakshatra nakshatra,
        @NotNull @FutureOrPresent LocalDate offeringDate,
        @NotEmpty List<@Valid @NotNull OrderItemRequest> items) {}
