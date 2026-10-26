package com.edstem.tdbvazhipadu.order.exception;

import com.edstem.tdbvazhipadu.common.exception.BusinessRuleException;
import com.edstem.tdbvazhipadu.offering.entity.OfferingType;
import java.time.LocalDate;

public class DailyLimitExceededException extends BusinessRuleException {

    public DailyLimitExceededException(OfferingType offeringType, LocalDate offeringDate, long remaining) {
        super("Only " + remaining + " " + offeringType + " left for " + offeringDate);
    }
}
