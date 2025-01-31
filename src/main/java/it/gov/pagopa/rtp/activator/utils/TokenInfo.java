package it.gov.pagopa.rtp.activator.utils;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import reactor.core.publisher.Mono;

public final class TokenInfo {

  private TokenInfo() {
  }

  public static Mono<String> getTokenSubject() {
    return ReactiveSecurityContextHolder.getContext().map(ctx -> ctx.getAuthentication())
        .map(auth -> auth.getPrincipal())
        .cast(Jwt.class)
        .map(jwt -> jwt.getSubject())
        .switchIfEmpty(Mono.error(new IllegalStateException("Subject not found")));
  }
}
