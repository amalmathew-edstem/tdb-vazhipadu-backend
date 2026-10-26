package com.edstem.tdbvazhipadu.order.dto;

import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record OrderSearchCriteria(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate offeringDate,
        OrderStatus status,
        @Pattern(regexp = "^\\+?[0-9]{10,14}$", message = "must be 10 to 14 digits, optionally starting with +")
                String phoneNumber) {}
