package com.edstem.tdbvazhipadu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class StaffAccountConfig {

    public static final String STAFF_ROLE = "STAFF";

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService staffAccounts(StaffSecurityProperties properties) {
        return new InMemoryUserDetailsManager(properties.staff().stream()
                .map(account -> User.withUsername(account.username())
                        .password(account.passwordHash())
                        .roles(STAFF_ROLE)
                        .build())
                .toArray(UserDetails[]::new));
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService staffAccounts, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(staffAccounts);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }
}
