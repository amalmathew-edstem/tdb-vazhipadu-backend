package com.edstem.tdbvazhipadu.collection.service;

import com.edstem.tdbvazhipadu.collection.dto.DailyCollectionResponse;
import com.edstem.tdbvazhipadu.collection.dto.OfferingCollection;
import com.edstem.tdbvazhipadu.collection.repository.CollectionRepository;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionService {

    public static final ZoneId TEMPLE_ZONE = ZoneId.of("Asia/Kolkata");

    private static final BigDecimal NO_COLLECTION = new BigDecimal("0.00");

    private final CollectionRepository collectionRepository;

    @Transactional(readOnly = true)
    public DailyCollectionResponse daily(LocalDate date) {
        Instant from = date.atStartOfDay(TEMPLE_ZONE).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(TEMPLE_ZONE).toInstant();
        List<OfferingCollection> offerings = collectionRepository.collectByOffering(from, to, OrderStatus.CANCELLED);
        BigDecimal total = offerings.stream().map(OfferingCollection::amount).reduce(NO_COLLECTION, BigDecimal::add);
        return new DailyCollectionResponse(date, TEMPLE_ZONE.getId(), total, offerings);
    }
}
