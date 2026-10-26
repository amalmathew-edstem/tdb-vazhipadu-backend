package com.edstem.tdbvazhipadu.order.mapper;

import com.edstem.tdbvazhipadu.order.dto.OrderItemResponse;
import com.edstem.tdbvazhipadu.order.dto.OrderResponse;
import com.edstem.tdbvazhipadu.order.entity.OrderItem;
import com.edstem.tdbvazhipadu.order.entity.VazhipaduOrder;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(VazhipaduOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getDevoteeName(),
                order.getPhoneNumber(),
                order.getNakshatra(),
                order.getOfferingDate(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getItems().stream().map(this::toItemResponse).toList());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getOffering().getId(),
                item.getOffering().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal());
    }
}
