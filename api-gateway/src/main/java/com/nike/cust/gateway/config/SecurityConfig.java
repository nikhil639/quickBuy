package com.nike.cust.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth

                        // ── PUBLIC ─────────────────────────────────────────────────────────
                        .pathMatchers("/api/users/register", "/api/users/login").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ── ADMIN ──────────────────────────────────────────────────────────
                        // Metrics & full actuator access
                        .pathMatchers("/actuator/**").hasRole("ADMIN")
                        // User management — list all users
                        .pathMatchers(HttpMethod.GET, "/api/users", "/api/users/**").hasRole("ADMIN")
                        // Hard deletes on the product catalog
                        .pathMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // ── SELLER ─────────────────────────────────────────────────────────
                        // Create new product listings
                        .pathMatchers(HttpMethod.POST, "/api/products").hasAnyRole("SELLER", "ADMIN")
                        // Edit product details
                        .pathMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        // Update inventory / stock quantity
                        .pathMatchers(HttpMethod.PATCH, "/api/products/**").hasAnyRole("SELLER", "ADMIN")

                        // ── CUSTOMER ───────────────────────────────────────────────────────
                        // Place and manage orders
                        .pathMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("CUSTOMER", "ADMIN")
                        .pathMatchers(HttpMethod.GET, "/api/orders/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/orders/**").hasAnyRole("CUSTOMER", "ADMIN")

                        // Anything else — valid token required
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(jwtSecret.getBytes(), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }

    // Reads the "authorities" claim (e.g. ["ROLE_SELLER"]) from the JWT
    // and converts it into Spring Security GrantedAuthority objects
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("authorities");
        authoritiesConverter.setAuthorityPrefix("");  // already prefixed as "ROLE_*" in the token

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
