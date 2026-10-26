package com.edstem.tdbvazhipadu.order.exception;

import com.edstem.tdbvazhipadu.common.exception.BusinessRuleException;

public class InactiveOfferingException extends BusinessRuleException {

    public InactiveOfferingException(String offeringName) {
        super("Offering " + offeringName + " is not active and cannot be ordered");
    }
}
