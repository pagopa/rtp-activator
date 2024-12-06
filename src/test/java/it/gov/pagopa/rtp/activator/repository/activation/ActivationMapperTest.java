package it.gov.pagopa.rtp.activator.repository.activation;

import org.junit.jupiter.api.Test;

import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.domain.payer.PayerID;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationEntity;
import it.gov.pagopa.rtp.activator.repository.activation.ActivationMapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

class ActivationMapperTest {

    private ActivationMapper mapper = new ActivationMapper();

    @Test
    void testToDomain() {
        ActivationEntity activationEntity = new ActivationEntity();
        activationEntity.setId(UUID.randomUUID().toString());
        activationEntity.setRtpSpId("RTP_SP_ID");
        activationEntity.setFiscalCode("FISCAL_CODE");
        activationEntity.setEffectiveActivationDate(Instant.ofEpochSecond(1732517304));

        Payer payer = mapper.toDomain(activationEntity);

        assertNotNull(payer);
        assertEquals(activationEntity.getId(), payer.payerID().getId().toString());
        assertEquals(activationEntity.getRtpSpId(), payer.rtpSpId());
        assertEquals(activationEntity.getFiscalCode(), payer.fiscalCode());
        assertEquals(activationEntity.getEffectiveActivationDate(), payer.effectiveActivationDate());
    }

    @Test
    void testToDbEntity() {
        PayerID payerID = new PayerID(UUID.randomUUID());
        Payer payer = new Payer(payerID, "RTP_SP_ID", "FISCAL_CODE", Instant.ofEpochSecond(1732517304));

        ActivationEntity activationEntity = mapper.toDbEntity(payer);

        assertNotNull(activationEntity);
        assertEquals(payer.payerID().getId().toString(), activationEntity.getId());
        assertEquals(payer.rtpSpId(), activationEntity.getRtpSpId());
        assertEquals(payer.fiscalCode(), activationEntity.getFiscalCode());
        assertEquals(payer.effectiveActivationDate(), activationEntity.getEffectiveActivationDate());
    }
}
