package it.gov.pagopa.rtp.activator.utils;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Users {

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = "writer", roles = "write_rtp_activations")
    public @interface RtpWriter { }

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = "reader", roles = "read_rtp_activations")
    public @interface RtpReader { }
}
