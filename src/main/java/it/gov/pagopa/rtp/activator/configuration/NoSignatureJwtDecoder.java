package it.gov.pagopa.rtp.activator.configuration;

import static java.util.Collections.emptyMap;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.util.Objects;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

public class NoSignatureJwtDecoder implements JwtDecoder {

  private final OAuth2TokenValidator<Jwt> verifier = JwtValidators.createDefault();
  private final MappedJwtClaimSetConverter claimMapper =
      MappedJwtClaimSetConverter.withDefaults(emptyMap());

  @Override
  public Jwt decode(String token) throws JwtException {
    try {
      final var parsedToken = JWTParser.parse(token);
      // convert nimbus token to spring Jwt
      final var convertedClaims = claimMapper.convert(parsedToken.getJWTClaimsSet().toJSONObject());

      final var jwt =
          Jwt.withTokenValue(parsedToken.getParsedString())
              .headers(headers -> headers.putAll(parsedToken.getHeader().toJSONObject()))
              .claims(claims -> claims.putAll(convertedClaims))
              .build();

      final var validation = verifier.validate(jwt);
      if (validation.hasErrors()) {
        final var description =
            validation.getErrors().stream()
                .filter(it -> Objects.nonNull(it) && !it.getDescription().isEmpty())
                .map(OAuth2Error::getDescription)
                .findFirst()
                .orElse("Invalid jwt token");
        throw new JwtValidationException(description, validation.getErrors());
      }

      return jwt;
    } catch (ParseException e) {
      throw new BadJwtException(e.getMessage());
    }
  }
}
