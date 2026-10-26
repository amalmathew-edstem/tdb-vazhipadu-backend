package com.edstem.tdbvazhipadu.support;

import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestStaffAccount {

    public static final String USERNAME = "test-staff";
    public static final String PASSWORD = UUID.randomUUID().toString();

    private static final String PASSWORD_HASH = new BCryptPasswordEncoder().encode(PASSWORD);
    private static final String SIGNING_KEY = UUID.randomUUID().toString() + UUID.randomUUID();

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext context) {
            context.getEnvironment()
                    .getPropertySources()
                    .addFirst(new MapPropertySource(
                            "testStaffAccount",
                            Map.of(
                                    "tdb.security.jwt-secret", SIGNING_KEY,
                                    "tdb.security.staff[0].username", USERNAME,
                                    "tdb.security.staff[0].password-hash", PASSWORD_HASH)));
        }
    }
}
