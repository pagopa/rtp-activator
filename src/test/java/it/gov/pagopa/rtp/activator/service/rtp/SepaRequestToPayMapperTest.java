package it.gov.pagopa.rtp.activator.service.rtp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class SepaRequestToPayMapperTest {

  @Mock private Rtp rtp;

  @InjectMocks private SepaRequestToPayMapper sepaRequestToPayMapper;

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
    BigDecimal amount = new BigDecimal("99999999999");
    LocalDateTime savingDateTime = LocalDateTime.now();
    LocalDate expiryDate = LocalDate.now().plusDays(5);
    String description = "Pagamento TARI";
    String noticeNumber = "123456";
    String payTrxRef = "payTrxRef123";
    String flgConf = "flgConf123";

    when(rtp.resourceID()).thenReturn(resourceId);
    when(rtp.payerId()).thenReturn(payerId);
    when(rtp.payeeId()).thenReturn(payeeId);
    when(rtp.payeeName()).thenReturn(payeeName);
    when(rtp.rtpSpId()).thenReturn(rtpSpId);
    when(rtp.iban()).thenReturn(iban);
    when(rtp.endToEndId()).thenReturn(endToEndId);
    when(rtp.amount()).thenReturn(amount);
    when(rtp.savingDateTime()).thenReturn(savingDateTime);
    when(rtp.expiryDate()).thenReturn(expiryDate);
    when(rtp.description()).thenReturn(description);
    when(rtp.noticeNumber()).thenReturn(noticeNumber);
    when(rtp.payTrxRef()).thenReturn(payTrxRef);
    when(rtp.flgConf()).thenReturn(flgConf);

    SepaRequestToPayRequestResourceDto result = sepaRequestToPayMapper.toRequestToPay(rtp);

    assertNotNull(result);
    assertEquals(resourceId.getId().toString(), result.getResourceId());
    assertEquals("http://spsrtp.api.cstar.pagopa.it", result.getCallbackUrl().toString());
    assertEquals(
        resourceId.getId().toString(),
        result.getDocument().getCdtrPmtActvtnReq().getGrpHdr().getMsgId());
    assertTrue(
        result
            .getDocument()
            .getCdtrPmtActvtnReq()
            .getPmtInf()
            .get(0)
            .getCdtTrfTx()
            .get(0)
            .getRmtInf()
            .getUstrd()
            .contains(description));
  }
}
