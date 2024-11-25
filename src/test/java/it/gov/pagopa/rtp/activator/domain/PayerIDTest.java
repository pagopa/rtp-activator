package it.gov.pagopa.rtp.activator.domain;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

public class PayerIDTest {

    @Test
    public void testCreateNew() {
        PayerID payerID = PayerID.createNew();
        assertNotNull(payerID);
        assertNotNull(payerID.getId());
    }

    @Test
    public void testConstructor() {
        UUID uuid = UUID.randomUUID();
        PayerID payerID = new PayerID(uuid);
        assertNotNull(payerID);
        assertEquals(uuid, payerID.getId());
    }
}
