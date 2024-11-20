package it.gov.pagopa.rtp.activator;

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

public final class JwtTestUtils {

    public static String generateToken(String subject, String... roles) throws JOSEException {
        return generateToken(subject, new Date(new Date().getTime() + 60 * 60 * 1000), roles); // 1 hour
    }

    public static String generateExpiredToken(String subject, String... roles) throws JOSEException {
        return generateToken(subject, new Date(new Date().getTime() - 60 * 60 * 1000), roles); // 1 hour ago
    }

    private static String generateToken(String subject, Date expirationTime, String... roles) throws JOSEException {
        // Create HMAC signer
        JWSSigner signer = new MACSigner(
                IntStream.range(0, 256).mapToObj(Integer::toString).collect(Collectors.joining())
        );

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(subject)
                .claim("groups", roles)
                .issuer("pagopa.it")
                .expirationTime(expirationTime) // 1 hour expiration
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claimsSet);

        // Apply the HMAC signature
        signedJWT.sign(signer);

        // Serialize to compact form
        return signedJWT.serialize();
    }

}
