package com.edstem.tdbvazhipadu.order.exception;

import com.edstem.tdbvazhipadu.common.exception.BusinessRuleException;
import com.edstem.tdbvazhipadu.offering.entity.OfferingType;

public class InactiveOfferingException extends BusinessRuleException {

    public InactiveOfferingException(OfferingType offeringType) {
        super("Offering " + offeringType + " is not active and cannot be ordered");
    }
}
