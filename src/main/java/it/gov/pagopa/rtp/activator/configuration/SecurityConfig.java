package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;


@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(proxyTargetClass = true) // allows to use @PreAuthorize with roles
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtAuthenticationConverter jwtConverter
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(it -> it
                        .pathMatchers("/actuator/**", "/rtps")
                        .permitAll()
                        .anyExchange()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(it -> it.jwtAuthenticationConverter(jwtConverter))
                )
                .build();
    }

    @Bean
    ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        final var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("groups"); // Map "groups" claim to authorities
        authoritiesConverter.setAuthorityPrefix("ROLE_"); // Add "ROLE_" prefix for Spring Security

        final var reactiveConverter = new ReactiveJwtAuthenticationConverter();
        reactiveConverter.setJwtGrantedAuthoritiesConverter(
                new ReactiveJwtGrantedAuthoritiesConverterAdapter(authoritiesConverter)
        );
        return reactiveConverter;
    }

    @Bean
    ReactiveJwtDecoder jwtDecoder() {
        final var decoder = new NoSignatureJwtDecoder();
        return token -> Mono.fromSupplier(() -> decoder.decode(token));
    }

}
