package it.gov.pagopa.rtp.activator.domain.payer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

class PayerIDTest {

    @Test
    void testCreateNew() {
        PayerID payerID = PayerID.createNew();
        assertNotNull(payerID);
        assertNotNull(payerID.getId());
    }

    @Test
    void testConstructor() {
        UUID uuid = UUID.randomUUID();
        PayerID payerID = new PayerID(uuid);
        assertNotNull(payerID);
        assertEquals(uuid, payerID.getId());
    }
}
