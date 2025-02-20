package it.gov.pagopa.rtp.activator.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import static org.assertj.core.api.Assertions.assertThat;

class MSALConfigurationTest {

    @Test
    void shouldRegisterReflectionHints() {
        RuntimeHints hints = new RuntimeHints();
        MSALConfiguration.MSALRuntimeHints registrar = new MSALConfiguration.MSALRuntimeHints();
        
        registrar.registerHints(hints, getClass().getClassLoader());


        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse")))
            .accepts(hints);

        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse"))
            .withMemberCategories(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS))
            .accepts(hints);
        
        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse"))
            .withMemberCategories(MemberCategory.INVOKE_DECLARED_METHODS))
            .accepts(hints);
        
        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse"))
            .withMemberCategories(MemberCategory.DECLARED_FIELDS))
            .accepts(hints);

        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse")))
            .accepts(hints);

        assertThat(RuntimeHintsPredicates.reflection().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse$ManagedIdentityResponseBuilder")))
            .accepts(hints);
    }

    @Test
    void shouldRegisterSerializationHints() {
        RuntimeHints hints = new RuntimeHints();
        MSALConfiguration.MSALRuntimeHints registrar = new MSALConfiguration.MSALRuntimeHints();
        
        registrar.registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.serialization().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityErrorResponse")))
            .accepts(hints);

        assertThat(RuntimeHintsPredicates.serialization().onType(
            TypeReference.of("com.microsoft.aad.msal4j.ManagedIdentityResponse")))
            .accepts(hints);
    }

    @Test
    void shouldRegisterAllRequiredTypes() {
        RuntimeHints hints = new RuntimeHints();
        MSALConfiguration.MSALRuntimeHints registrar = new MSALConfiguration.MSALRuntimeHints();
        
        registrar.registerHints(hints, getClass().getClassLoader());

        String[] requiredTypes = {
            "com.microsoft.aad.msal4j.ManagedIdentityErrorResponse",
            "com.microsoft.aad.msal4j.ManagedIdentityResponse",
            "com.microsoft.aad.msal4j.ManagedIdentityResponse$ManagedIdentityResponseBuilder"
        };

        for (String type : requiredTypes) {
            assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(type)))
                .as("Type " + type + " should be registered for reflection")
                .accepts(hints);
        }
    }
}
