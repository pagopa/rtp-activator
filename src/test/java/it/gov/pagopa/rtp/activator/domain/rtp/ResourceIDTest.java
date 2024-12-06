package it.gov.pagopa.rtp.activator.domain.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class ResourceIDTest {
      @Test
    void testCreateNew() {
        ResourceID resourceID = ResourceID.createNew();
        assertNotNull(resourceID);
        assertNotNull(resourceID.getId());
    }

    @Test
    void testConstructor() {
        UUID uuid = UUID.randomUUID();
        ResourceID resourceID = new ResourceID(uuid);
        assertNotNull(resourceID);
        assertEquals(uuid, resourceID.getId());
    }
    
}
