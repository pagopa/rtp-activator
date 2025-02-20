package it.gov.pagopa.rtp.activator.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints(MSALConfiguration.MSALRuntimeHints.class)
public class MSALConfiguration {

    private MSALConfiguration() {
    }
    static class MSALRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.reflection()
                .registerType(TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse"),
                    builder -> builder.withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    ))
                .registerType(TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse"),
                    builder -> builder.withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    ))
                .registerType(TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse$ManagedIdentityResponseBuilder"),
                    builder -> builder.withMembers(
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INVOKE_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS
                    ));

            // Register serialization hints
            hints.serialization()
                .registerType(TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse"))
                .registerType(TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse"));
        }
    }
}