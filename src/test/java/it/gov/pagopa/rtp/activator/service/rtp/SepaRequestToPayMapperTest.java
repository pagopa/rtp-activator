package it.gov.pagopa.rtp.activator.service.rtp;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

class SepaRequestToPayMapperTest {

    @InjectMocks
    private SepaRequestToPayMapper sepaRequestToPayMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testToEpcRequestToPay() {
        ResourceID resourceId = ResourceID.createNew();
        String payerId = "payerId123";
        String payeeId = "payeeId123";
        String payeeName = "Comune di Bugliano";
        String rtpSpId = "BIC123456";
        String iban = "IT60X0542811101000000123456";
        BigDecimal amount = new BigDecimal("99999999999");
        LocalDateTime savingDateTime = LocalDateTime.now();
        LocalDate expiryDate = LocalDate.now().plusDays(5);
        String description = "Pagamento TARI";
        String noticeNumber = "123456";
        String payTrxRef = "ABC/124";
        String flgConf = "flgConf123";
        String payerName = "John Doe";
        String subject = "subject";

        Rtp nRtp = Rtp.builder().resourceID(resourceId).payerId(payerId).payerName(payerName).payeeId(payeeId)
                .payeeName(payeeName).rtpSpId(rtpSpId).iban(iban).amount(amount)
                .savingDateTime(savingDateTime).expiryDate(expiryDate).description(description).subject(subject)
                .noticeNumber(noticeNumber).payTrxRef(payTrxRef).flgConf(flgConf).build();

        var result = sepaRequestToPayMapper.toEpcRequestToPay(nRtp);

        assertNotNull(result);
        assertEquals(resourceId.getId().toString(), result.getResourceId());
        assertEquals("http://spsrtp.api.cstar.pagopa.it", result.getCallbackUrl().toString());
        assertEquals(resourceId.getId().toString(), result.getDocument().getCdtrPmtActvtnReq().getGrpHdr().getMsgId());
        assertTrue(result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0).getCdtTrfTx().get(0).getRmtInf()
                .getUstrd().get(1).contains(description));


     
        // Verify group header
        var grpHdr = result.getDocument().getCdtrPmtActvtnReq().getGrpHdr();
        assertEquals(nRtp.resourceID().getId().toString(), grpHdr.getMsgId());
        assertEquals(nRtp.savingDateTime().toString(), grpHdr.getCreDtTm());


        // Verify payment information
        var pmtInf = result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0);
        assertEquals(nRtp.noticeNumber(), pmtInf.getPmtInfId());
        assertTrue(pmtInf.getXpryDt().toString().contains(nRtp.expiryDate().toString()));
        
        // Verify debtor information
        assertEquals(nRtp.payerName(), pmtInf.getDbtr().getNm());
        
        // Verify credit transfer transaction
        var cdtTrfTx = pmtInf.getCdtTrfTx().get(0);
        assertEquals(nRtp.noticeNumber(), cdtTrfTx.getPmtId().getEndToEndId());
        
        // Verify creditor information
        assertEquals(nRtp.payeeName(), cdtTrfTx.getCdtr().getNm());
        assertTrue(cdtTrfTx.getCdtrAcct().getId().toString().contains(nRtp.iban()));

        // Verify remittance information
        var rmtInf = cdtTrfTx.getRmtInf();
        assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.subject()));
        assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.noticeNumber()));
        assertTrue(rmtInf.getUstrd().get(1).contains(nRtp.description()));
        
         // Verify instruction for creditor agent
         var instrForCdtrAgt = cdtTrfTx.getInstrForCdtrAgt();
         assertEquals("ATR113/" + nRtp.payTrxRef(), instrForCdtrAgt.get(0).getInstrInf());
         assertEquals(nRtp.flgConf(), instrForCdtrAgt.get(1).getInstrInf());
         
         // Verify callback URL
         assertEquals("http://spsrtp.api.cstar.pagopa.it", result.getCallbackUrl().toString());
     
    }
}


