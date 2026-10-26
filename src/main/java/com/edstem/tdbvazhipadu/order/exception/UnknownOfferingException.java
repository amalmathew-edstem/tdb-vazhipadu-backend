package com.edstem.tdbvazhipadu.order.exception;

import com.edstem.tdbvazhipadu.common.exception.BusinessRuleException;

public class UnknownOfferingException extends BusinessRuleException {

    public UnknownOfferingException(Long offeringId) {
        super("Offering " + offeringId + " does not exist");
    }
}
