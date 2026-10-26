package com.edstem.tdbvazhipadu.collection.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record DailyCollectionResponse(
        LocalDate date, String timeZone, BigDecimal totalAmount, List<OfferingCollection> offerings) {}
