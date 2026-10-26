package com.edstem.tdbvazhipadu.auth.service;

import com.edstem.tdbvazhipadu.auth.dto.LoginRequest;
import com.edstem.tdbvazhipadu.auth.dto.TokenResponse;
import com.edstem.tdbvazhipadu.config.JwtConfig;
import com.edstem.tdbvazhipadu.config.StaffSecurityProperties;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final StaffSecurityProperties properties;
    private final Clock clock;

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(properties.tokenTtl()))
                .claim(
                        JwtConfig.ROLES_CLAIM,
                        authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList())
                .build();
        JwsHeader header = JwsHeader.with(JwtConfig.SIGNING_ALGORITHM).build();
        String token =
                jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, TOKEN_TYPE, properties.tokenTtl().toSeconds());
    }
}
