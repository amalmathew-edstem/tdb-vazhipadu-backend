package com.edstem.tdbvazhipadu.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {"/actuator/health", "/actuator/info", "/error"};

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver)
            throws Exception {
        AuthenticationEntryPoint entryPoint = (request, response, exception) ->
                exceptionResolver.resolveException(request, response, null, exception);
        AccessDeniedHandler accessDeniedHandler = (request, response, exception) ->
                exceptionResolver.resolveException(request, response, null, exception);
        return http.csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests.requestMatchers(HttpMethod.POST, "/api/auth/login")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders")
                        .hasRole(StaffAccountConfig.STAFF_ROLE)
                        .requestMatchers(HttpMethod.POST, "/api/orders")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders/*/cancel")
                        .hasRole(StaffAccountConfig.STAFF_ROLE)
                        .requestMatchers(HttpMethod.GET, "/api/collections/daily")
                        .hasRole(StaffAccountConfig.STAFF_ROLE)
                        .requestMatchers(HttpMethod.GET, "/api/orders/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/offerings", "/api/offerings/*")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/offerings")
                        .hasRole(StaffAccountConfig.STAFF_ROLE)
                        .requestMatchers(HttpMethod.PUT, "/api/offerings/*")
                        .hasRole(StaffAccountConfig.STAFF_ROLE)
                        .requestMatchers(PUBLIC_ENDPOINTS)
                        .permitAll()
                        .anyRequest()
                        .denyAll())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(entryPoint).accessDeniedHandler(accessDeniedHandler))
                .build();
    }
}
