package it.gov.pagopa.rtp.activator.service.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

class SepaRequestToPayMapperTest {

    @Mock
    private Rtp rtp;

    @InjectMocks
    private SepaRequestToPayMapper sepaRequestToPayMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testToRequestToPay() {
        ResourceID resourceId = ResourceID.createNew();
        String payerId = "payerId123";
        String payeeId = "payeeId123";
        String payeeName = "Mario Rossi";
        String rtpSpId = "BIC123456";
        String iban = "IT60X0542811101000000123456";
        String endToEndId = "endToEndId123";
        BigDecimal amount = BigDecimal.valueOf(10);
        LocalDateTime savingDateTime = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        String description = "Pagamento TARI";
        String noticeNumber = "123456";
        String payTrxRef = "payTrxRef123";
        String flgConf = "flgConf123";

        when(rtp.getResourceID()).thenReturn(resourceId);
        when(rtp.getPayerId()).thenReturn(payerId);
        when(rtp.getPayeeId()).thenReturn(payeeId);
        when(rtp.getPayeeName()).thenReturn(payeeName);
        when(rtp.getRtpSpId()).thenReturn(rtpSpId);
        when(rtp.getIban()).thenReturn(iban);
        when(rtp.getEndToEndId()).thenReturn(endToEndId);
        when(rtp.getAmount()).thenReturn(amount);
        when(rtp.getSavingDateTime()).thenReturn(savingDateTime);
        when(rtp.getExpiryDate()).thenReturn(expiryDate);
        when(rtp.getDescription()).thenReturn(description);
        when(rtp.getNoticeNumber()).thenReturn(noticeNumber);
        when(rtp.getPayTrxRef()).thenReturn(payTrxRef);
        when(rtp.getFlgConf()).thenReturn(flgConf);

        SepaRequestToPayRequestResourceDto result = sepaRequestToPayMapper.toRequestToPay(rtp);

        assertNotNull(result);
        assertEquals(resourceId.getId().toString(), result.getResourceId());
        assertEquals("http://spsrtp.api.cstar.pagopa.it", result.getCallbackUrl().toString());
        assertEquals(resourceId.getId().toString(), result.getDocument().getCdtrPmtActvtnReq().getGrpHdr().getMsgId());
        assertTrue(result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0).getCdtTrfTx().get(0).getRmtInf()
                .getUstrd().contains(description));
    }
}
