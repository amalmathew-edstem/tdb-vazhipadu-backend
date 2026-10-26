package com.edstem.tdbvazhipadu.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " " + id + " was not found");
    }
}
