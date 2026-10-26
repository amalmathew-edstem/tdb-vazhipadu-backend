package com.edstem.tdbvazhipadu.order.exception;

import com.edstem.tdbvazhipadu.common.exception.BusinessRuleException;
import com.edstem.tdbvazhipadu.order.entity.OrderStatus;

public class OrderNotCancellableException extends BusinessRuleException {

    public OrderNotCancellableException(Long orderId, OrderStatus status) {
        super("Order " + orderId + " is " + status + " and cannot be cancelled");
    }
}
