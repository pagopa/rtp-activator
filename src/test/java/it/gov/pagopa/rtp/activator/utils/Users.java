package it.gov.pagopa.rtp.activator.utils;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Users {

    public static final String SERVICE_PROVIDER_ID = "984500A9EB6B07AC2G71";
    public static final String SUBJECT = "PagoPA";
    public static final String ACTIVATION_WRITE_ROLE = "write_rtp_activations";
    public static final String ACTIVATION_READ_ROLE = "read_rtp_activations";
    public static final String SENDER_WRITER_ROLE = "write_rtp_send";

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = SERVICE_PROVIDER_ID, roles = ACTIVATION_WRITE_ROLE)
    public @interface RtpWriter {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = SERVICE_PROVIDER_ID, roles = ACTIVATION_READ_ROLE)
    public @interface RtpReader {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithSecurityContext(factory = RtpSenderWriterSecurityContextFactory.class)
    public @interface RtpSenderWriter {
        String subject() default SUBJECT;
        String[] roles() default { SENDER_WRITER_ROLE };
    }

    public static class RtpSenderWriterSecurityContextFactory implements WithSecurityContextFactory<RtpSenderWriter> {
        @Override
        public SecurityContext createSecurityContext(RtpSenderWriter annotation) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();

            List<GrantedAuthority> authorities = Arrays.stream(annotation.roles())
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            var claims = new HashMap<String, Object>();
            claims.put("sub", annotation.subject());

            Jwt jwt = Jwt.withTokenValue("test-token")
                    .header("alg", "none")
                    .claims(c -> c.putAll(claims))
                    .build();

            JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, authorities);
            context.setAuthentication(auth);

            return context;
        }
    }

}
