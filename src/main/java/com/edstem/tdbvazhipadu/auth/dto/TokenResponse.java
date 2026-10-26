package com.edstem.tdbvazhipadu.auth.dto;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
