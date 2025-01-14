package it.gov.pagopa.rtp.activator.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class JwtUtils {

  public static String generateToken(String subject, String... roles) throws JOSEException {
    return generateToken(subject, new Date(new Date().getTime() + 60 * 60 * 1000), roles); // 1 hour
  }

  public static String generateExpiredToken(String subject, String... roles) throws JOSEException {
    return generateToken(
        subject, new Date(new Date().getTime() - 60 * 60 * 1000), roles); // 1 hour ago
  }

  private static String generateToken(String subject, Date expirationTime, String... roles)
      throws JOSEException {
    JWSSigner signer =
        new MACSigner(
            IntStream.range(0, 256).mapToObj(Integer::toString).collect(Collectors.joining()));

    JWTClaimsSet claimsSet =
        new JWTClaimsSet.Builder()
            .subject(subject)
            .claim("groups", roles)
            .issuer("pagopa.it")
            .expirationTime(expirationTime)
            .build();

    SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);

    signedJWT.sign(signer);
    return signedJWT.serialize();
  }
}
