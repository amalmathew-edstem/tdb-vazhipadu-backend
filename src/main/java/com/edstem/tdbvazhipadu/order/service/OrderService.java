package com.edstem.tdbvazhipadu.order.service;

import com.edstem.tdbvazhipadu.common.exception.ResourceNotFoundException;
import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.offering.repository.OfferingRepository;
import com.edstem.tdbvazhipadu.order.dto.CreateOrderRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderItemRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderResponse;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import com.edstem.tdbvazhipadu.order.exception.InactiveOfferingException;
import com.edstem.tdbvazhipadu.order.exception.UnknownOfferingException;
import com.edstem.tdbvazhipadu.order.mapper.OrderMapper;
import com.edstem.tdbvazhipadu.order.repository.OrderRepository;
import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OfferingRepository offeringRepository;
    private final OrderMapper orderMapper;
    private final Clock clock;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Map<Long, Offering> offeringsById = findOfferings(request);
        VazhipaduOrder order = new VazhipaduOrder(
                request.devoteeName(),
                request.phoneNumber(),
                request.nakshatra(),
                request.offeringDate(),
                clock.instant());
        for (OrderItemRequest item : request.items()) {
            order.addItem(orderableOffering(offeringsById, item.offeringId()), item.quantity());
        }
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return orderRepository
                .findWithItemsById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private Map<Long, Offering> findOfferings(CreateOrderRequest request) {
        Set<Long> offeringIds =
                request.items().stream().map(OrderItemRequest::offeringId).collect(Collectors.toSet());
        return offeringRepository.findAllById(offeringIds).stream()
                .collect(Collectors.toMap(Offering::getId, Function.identity()));
    }

    private static Offering orderableOffering(Map<Long, Offering> offeringsById, Long offeringId) {
        Offering offering = offeringsById.get(offeringId);
        if (offering == null) {
            throw new UnknownOfferingException(offeringId);
        }
        if (!offering.isActive()) {
            throw new InactiveOfferingException(offering.getName());
        }
        return offering;
    }
}
