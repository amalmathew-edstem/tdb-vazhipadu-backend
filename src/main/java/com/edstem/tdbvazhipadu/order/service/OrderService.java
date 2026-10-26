package com.edstem.tdbvazhipadu.order.service;

import com.edstem.tdbvazhipadu.common.exception.ResourceNotFoundException;
import com.edstem.tdbvazhipadu.offering.entity.Offering;
import com.edstem.tdbvazhipadu.offering.repository.OfferingRepository;
import com.edstem.tdbvazhipadu.order.dto.CreateOrderRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderItemRequest;
import com.edstem.tdbvazhipadu.order.dto.OrderResponse;
import com.edstem.tdbvazhipadu.order.dto.OrderSearchCriteria;
import com.edstem.tdbvazhipadu.order.dto.OrderSummaryResponse;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import com.edstem.tdbvazhipadu.order.exception.DailyLimitExceededException;
import com.edstem.tdbvazhipadu.order.exception.InactiveOfferingException;
import com.edstem.tdbvazhipadu.order.exception.UnknownOfferingException;
import com.edstem.tdbvazhipadu.order.mapper.OrderMapper;
import com.edstem.tdbvazhipadu.order.repository.OrderRepository;
import com.edstem.tdbvazhipadu.order.repository.OrderSpecifications;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Sort SEARCH_ORDER = Sort.by(Sort.Order.desc("offeringDate"), Sort.Order.desc("id"));

    private final OrderRepository orderRepository;
    private final OfferingRepository offeringRepository;
    private final OrderMapper orderMapper;
    private final Clock clock;

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Map<Long, Integer> requestedQuantities = request.items().stream()
                .collect(Collectors.toMap(OrderItemRequest::offeringId, OrderItemRequest::quantity, Integer::sum));
        List<Offering> dailyLimited = offeringRepository.lockDailyLimitedByIdIn(requestedQuantities.keySet());
        Map<Long, Offering> offeringsById = findOfferings(requestedQuantities.keySet());
        VazhipaduOrder order = new VazhipaduOrder(
                request.devoteeName(),
                request.phoneNumber(),
                request.nakshatra(),
                request.offeringDate(),
                clock.instant());
        for (OrderItemRequest item : request.items()) {
            order.addItem(orderableOffering(offeringsById, item.offeringId()), item.quantity());
        }
        for (Offering offering : dailyLimited) {
            enforceDailyLimit(offering, request.offeringDate(), requestedQuantities.get(offering.getId()));
        }
        return orderMapper.toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancel(Long id) {
        VazhipaduOrder order =
                orderRepository.findWithItemsById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
        order.cancel();
        return orderMapper.toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return orderRepository
                .findWithItemsById(id)
                .map(orderMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> search(OrderSearchCriteria criteria, Pageable pageable) {
        Pageable stablePage = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), SEARCH_ORDER);
        return orderRepository
                .findAll(OrderSpecifications.matching(criteria), stablePage)
                .map(orderMapper::toSummary);
    }

    private void enforceDailyLimit(Offering offering, LocalDate offeringDate, int requestedQuantity) {
        long booked = orderRepository.sumBookedQuantity(offering.getId(), offeringDate, OrderStatus.CANCELLED);
        long remaining = offering.getDailyLimit() - booked;
        if (requestedQuantity > remaining) {
            throw new DailyLimitExceededException(offering.getType(), offeringDate, Math.max(remaining, 0));
        }
    }

    private Map<Long, Offering> findOfferings(Set<Long> offeringIds) {
        return offeringRepository.findAllById(offeringIds).stream()
                .collect(Collectors.toMap(Offering::getId, Function.identity()));
    }

    private static Offering orderableOffering(Map<Long, Offering> offeringsById, Long offeringId) {
        Offering offering = offeringsById.get(offeringId);
        if (offering == null) {
            throw new UnknownOfferingException(offeringId);
        }
        if (!offering.isActive()) {
            throw new InactiveOfferingException(offering.getType());
        }
        return offering;
    }
}
