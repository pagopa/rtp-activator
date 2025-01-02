package it.gov.pagopa.rtp.activator.domain.payer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

class ActivationIDTest {

    @Test
    void testCreateNew() {
        ActivationID activationID = ActivationID.createNew();
        assertNotNull(activationID);
        assertNotNull(activationID.getId());
    }

    @Test
    void testConstructor() {
        UUID uuid = UUID.randomUUID();
        ActivationID activationID = new ActivationID(uuid);
        assertNotNull(activationID);
        assertEquals(uuid, activationID.getId());
    }
}
