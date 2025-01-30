package it.gov.pagopa.rtp.activator.telemetry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Annotation to enable MongoDB operation tracing.
 * Can be applied at both method and class level to indicate which MongoDB
 * operations should be traced using OpenTelemetry.
 *
 * When applied to:
 * - A method: Only that specific method will be traced
 * - A class: All MongoDB operations in that class will be traced
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TraceMongo {
}