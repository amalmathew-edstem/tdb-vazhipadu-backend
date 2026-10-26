package com.edstem.tdbvazhipadu.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("tdb.security")
public record StaffSecurityProperties(
        @NotBlank @Size(min = 32) String jwtSecret,
        @NotNull Duration tokenTtl,
        @NotEmpty List<@Valid StaffAccount> staff) {

    public record StaffAccount(@NotBlank String username, @NotBlank String passwordHash) {}
}
