package it.gov.pagopa.rtp.activator.utils;

import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Users {

    public static final String SERVICE_PROVIDER_ID = "984500A9EB6B07AC2G71";

    public static final String ACTIVATION_WRITE_ROLE = "write_rtp_activations";
    public static final String ACTIVATION_READ_ROLE = "read_rtp_activations";

    public static final String SENDER_WRITER_ROLDE = "write_rtp_send";

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = SERVICE_PROVIDER_ID, roles = ACTIVATION_WRITE_ROLE)
    public @interface RtpWriter {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = SERVICE_PROVIDER_ID, roles = ACTIVATION_READ_ROLE)
    public @interface RtpReader {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @WithMockUser(value = SERVICE_PROVIDER_ID, roles = SENDER_WRITER_ROLDE)
    public @interface RtpSenderWriter {
    }
}
