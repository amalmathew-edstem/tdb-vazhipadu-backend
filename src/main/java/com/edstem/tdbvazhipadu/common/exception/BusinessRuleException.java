package com.edstem.tdbvazhipadu.common.exception;

public abstract class BusinessRuleException extends RuntimeException {

    protected BusinessRuleException(String message) {
        super(message);
    }
}
