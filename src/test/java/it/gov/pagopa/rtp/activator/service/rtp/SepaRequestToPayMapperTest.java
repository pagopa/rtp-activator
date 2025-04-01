package it.gov.pagopa.rtp.activator.service.rtp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.gov.pagopa.rtp.activator.configuration.CallbackProperties;
import it.gov.pagopa.rtp.activator.configuration.PagoPaConfigProperties;
import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalCancellationReason1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto;

import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeIIDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalServiceLevel1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OriginalGroupInformation29EPC25922V30DS15RTPDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OriginalPaymentInstruction34EPC25922V30DS11Dto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SepaRequestToPayMapperTest {

  private CallbackProperties callbackProperties;
  private PagoPaConfigProperties pagoPaConfigProperties;

  private SepaRequestToPayMapper sepaRequestToPayMapper;


  @BeforeEach
  void setUp() {
    callbackProperties = new CallbackProperties(
        new CallbackProperties.UrlProperties("https://spsrtp.api.cstar.pagopa.it/send",
            "https://spsrtp.api.cstar.pagopa.it/cancel")
    );

    pagoPaConfigProperties = new PagoPaConfigProperties(
        new PagoPaConfigProperties.Anag("iban", "fiscalCode")
    );

    sepaRequestToPayMapper = new SepaRequestToPayMapper(callbackProperties, pagoPaConfigProperties);
  }



  @Test
  void testToEpcRequestToPay() {
    ResourceID resourceId = ResourceID.createNew();
    String payerId = "payerId123";
    String payeeId = "payeeId123";
    String payeeName = "Comune di Bugliano";
    String rtpSpId = "F4K3SP12";
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
    String serviceProviderCreditor = "serviceProviderDebtor";

    Rtp nRtp = Rtp.builder().resourceID(resourceId).payerId(payerId).payerName(payerName).payeeId(payeeId)
        .payeeName(payeeName).serviceProviderDebtor(rtpSpId).iban(iban).amount(amount)
        .savingDateTime(savingDateTime).expiryDate(expiryDate).description(description)
        .subject(subject)
        .noticeNumber(noticeNumber).payTrxRef(payTrxRef).flgConf(flgConf)
        .serviceProviderCreditor(serviceProviderCreditor).build();

    var result = sepaRequestToPayMapper.toEpcRequestToPay(nRtp);

    assertNotNull(result);
    assertEquals(resourceId.getId().toString(), result.getResourceId());
    assertEquals(this.callbackProperties.url().send(), result.getCallbackUrl().toString());
    assertEquals(resourceId.getId().toString().replace("-",""),
        result.getDocument().getCdtrPmtActvtnReq().getGrpHdr().getMsgId());
    assertTrue(result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0).getCdtTrfTx().get(0)
        .getRmtInf()
        .getUstrd().get(1).contains(description));

    // Verify group header
    var grpHdr = result.getDocument().getCdtrPmtActvtnReq().getGrpHdr();
    assertEquals(nRtp.resourceID().getId().toString().replace("-",""), grpHdr.getMsgId());
    assertEquals(nRtp.savingDateTime().toString(), grpHdr.getCreDtTm());

    // Verify payment information
    var pmtInf = result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0);
    assertEquals(nRtp.noticeNumber(), pmtInf.getPmtInfId());
    assertTrue(pmtInf.getXpryDt().toString().contains(nRtp.expiryDate().toString()));

    // Verify debtor information
    assertEquals(nRtp.payerName(), pmtInf.getDbtr().getNm());
    assertEquals(nRtp.serviceProviderDebtor(), pmtInf.getDbtrAgt().getFinInstnId().getBICFI());

    // Verify credit transfer transaction
    var cdtTrfTx = pmtInf.getCdtTrfTx().get(0);
    assertEquals(nRtp.noticeNumber(), cdtTrfTx.getPmtId().getEndToEndId());

    // Verify creditor information
    assertEquals(nRtp.payeeName(), cdtTrfTx.getCdtr().getNm());
    assertTrue(cdtTrfTx.getCdtrAcct().getId().toString().contains(pagoPaConfigProperties.anag().iban()));

    // Verify remittance information
    var rmtInf = cdtTrfTx.getRmtInf();
    assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.subject()));
    assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.noticeNumber()));
    assertTrue(rmtInf.getUstrd().get(1).contains(nRtp.description()));

    // Verify instruction for creditor agent
    var instrForCdtrAgt = cdtTrfTx.getInstrForCdtrAgt();
    assertEquals("ATR113/" + nRtp.payTrxRef(), instrForCdtrAgt.get(0).getInstrInf());
    assertEquals(nRtp.flgConf(), instrForCdtrAgt.get(1).getInstrInf());

    assertEquals(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID,
        cdtTrfTx.getCdtr().getId().getOrgId().getOthr().get(0).getSchmeNm().getCd());

    // Verify Service Provider Creditor
    assertEquals("BOID", cdtTrfTx.getCdtrAgt().getFinInstnId().getOthr().getSchmeNm().getCd());

    // Verify callback URL
    assertEquals(this.callbackProperties.url().send(), result.getCallbackUrl().toString());
  }

  @Test
  void testToEpcRequestToPayWithNonPspSpDebtor() {
    ResourceID resourceId = ResourceID.createNew();
    String payerId = "payerId123";
    String payeeId = "payeeId123";
    String payeeName = "Comune di Bugliano";
    String rtpSpId = "12345678911";
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
    String serviceProviderCreditor = "serviceProviderDebtor";

    Rtp nRtp = Rtp.builder().resourceID(resourceId).payerId(payerId).payerName(payerName).payeeId(payeeId)
        .payeeName(payeeName).serviceProviderDebtor(rtpSpId).iban(iban).amount(amount)
        .savingDateTime(savingDateTime).expiryDate(expiryDate).description(description)
        .subject(subject)
        .noticeNumber(noticeNumber).payTrxRef(payTrxRef).flgConf(flgConf)
        .serviceProviderCreditor(serviceProviderCreditor).build();

    var result = sepaRequestToPayMapper.toEpcRequestToPay(nRtp);

    assertNotNull(result);
    assertEquals(resourceId.getId().toString(), result.getResourceId());
    assertEquals(this.callbackProperties.url().send(), result.getCallbackUrl().toString());
    assertEquals(resourceId.getId().toString().replace("-",""),
        result.getDocument().getCdtrPmtActvtnReq().getGrpHdr().getMsgId());
    assertTrue(result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0).getCdtTrfTx().get(0)
        .getRmtInf()
        .getUstrd().get(1).contains(description));

    // Verify group header
    var grpHdr = result.getDocument().getCdtrPmtActvtnReq().getGrpHdr();
    assertEquals(nRtp.resourceID().getId().toString().replace("-",""), grpHdr.getMsgId());
    assertEquals(nRtp.savingDateTime().toString(), grpHdr.getCreDtTm());

    // Verify payment information
    var pmtInf = result.getDocument().getCdtrPmtActvtnReq().getPmtInf().get(0);
    assertEquals(nRtp.noticeNumber(), pmtInf.getPmtInfId());
    assertTrue(pmtInf.getXpryDt().toString().contains(nRtp.expiryDate().toString()));

    // Verify debtor information
    assertEquals(nRtp.payerName(), pmtInf.getDbtr().getNm());
    assertEquals(nRtp.serviceProviderDebtor(), pmtInf.getDbtrAgt().getFinInstnId().getOthr().getId());
    assertEquals("BOID", pmtInf.getDbtrAgt().getFinInstnId().getOthr().getSchmeNm().getCd());

    // Verify credit transfer transaction
    var cdtTrfTx = pmtInf.getCdtTrfTx().get(0);
    assertEquals(nRtp.noticeNumber(), cdtTrfTx.getPmtId().getEndToEndId());

    // Verify creditor information
    assertEquals(nRtp.payeeName(), cdtTrfTx.getCdtr().getNm());
    assertTrue(cdtTrfTx.getCdtrAcct().getId().toString().contains(pagoPaConfigProperties.anag().iban()));

    // Verify remittance information
    var rmtInf = cdtTrfTx.getRmtInf();
    assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.subject()));
    assertTrue(rmtInf.getUstrd().get(0).contains(nRtp.noticeNumber()));
    assertTrue(rmtInf.getUstrd().get(1).contains(nRtp.description()));

    // Verify instruction for creditor agent
    var instrForCdtrAgt = cdtTrfTx.getInstrForCdtrAgt();
    assertEquals("ATR113/" + nRtp.payTrxRef(), instrForCdtrAgt.get(0).getInstrInf());
    assertEquals(nRtp.flgConf(), instrForCdtrAgt.get(1).getInstrInf());


    assertEquals(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID,
        cdtTrfTx.getCdtr().getId().getOrgId().getOthr().get(0).getSchmeNm().getCd());

    // Verify Service Provider Creditor
    assertEquals("BOID", cdtTrfTx.getCdtrAgt().getFinInstnId().getOthr().getSchmeNm().getCd());

    
    // Verify callback URL
    assertEquals(this.callbackProperties.url().send(), result.getCallbackUrl().toString());
  }


  @Test
  void testToEpcRequestToCancel() {
    ResourceID resourceId = ResourceID.createNew();
    String payerId = "payerId123";
    String payeeId = "payeeId123";
    String payeeName = "Comune di Bugliano";
    String rtpSpId = "F4K3SP12";
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
    String serviceProviderCreditor = "serviceProviderCreditor";
    String serviceProviderDebtor = "serviceProviderDebtor";

    final var nRtp = Rtp.builder()
        .resourceID(resourceId)
        .payerId(payerId)
        .payerName(payerName)
        .payeeId(payeeId)
        .payeeName(payeeName)
        .serviceProviderDebtor(serviceProviderDebtor)
        .iban(iban)
        .amount(amount)
        .savingDateTime(savingDateTime)
        .expiryDate(expiryDate)
        .description(description)
        .subject(subject)
        .noticeNumber(noticeNumber)
        .payTrxRef(payTrxRef)
        .flgConf(flgConf)
        .serviceProviderCreditor(serviceProviderCreditor)
        .build();

    final var result = sepaRequestToPayMapper.toEpcRequestToCancel(nRtp);

    assertNotNull(result);
    assertEquals(resourceId.getId().toString(), result.getResourceId());

    final var originalPaymentInstruction = result.getDocument().getCstmrPmtCxlReq().getUndrlyg().getOrgnlPmtInfAndCxl().getFirst();
    assertEquals(resourceId.getId().toString(),
        originalPaymentInstruction.getPmtCxlId());
    assertEquals(resourceId.getId().toString(),
        originalPaymentInstruction.getOrgnlPmtInfId());
    assertEquals(resourceId.getId().toString().replace("-",""),
        originalPaymentInstruction.getOrgnlGrpInf().getOrgnlMsgId());
    assertEquals(savingDateTime.toString(),
        originalPaymentInstruction.getOrgnlGrpInf().getOrgnlCreDtTm());

    final var paymentTransaction = originalPaymentInstruction.getTxInf().getFirst();
    assertEquals(resourceId.getId().toString(),
        paymentTransaction.getCxlId());
    assertEquals(noticeNumber, paymentTransaction.getOrgnlEndToEndId());
    assertEquals(amount,
        paymentTransaction.getOrgnlTxRef().getAmt().getInstdAmt());
    assertEquals(String.valueOf(expiryDate),
        paymentTransaction.getOrgnlTxRef().getReqdExctnDt().getDt());
    assertEquals(subject,
        paymentTransaction.getOrgnlTxRef().getRmtInf().getUstrd());
    assertEquals(serviceProviderDebtor,
        paymentTransaction.getOrgnlTxRef().getDbtrAgt().getFinInstnId().getBICFI());
    assertEquals(pagoPaConfigProperties.anag().iban(),
        paymentTransaction.getOrgnlTxRef().getCdtrAcct().getId().getIBAN());

    final var paymentCancellationReason = paymentTransaction.getCxlRsnInf();
    assertEquals(ExternalCancellationReason1CodeDto.PAID,
        paymentCancellationReason.getRsn().getCd());
    assertEquals(payeeName,
        paymentCancellationReason.getOrgtr().getNm());
    assertEquals(pagoPaConfigProperties.anag().fiscalCode(),
        paymentCancellationReason.getOrgtr().getId().getOrgId().getOthr().getId());
    assertEquals(ExternalOrganisationIdentification1CodeEPC25922V30DS02Dto.BOID,
        paymentCancellationReason.getOrgtr().getId().getOrgId().getOthr().getSchmeNm().getCd());
    assertEquals("ATS005/ " + expiryDate,
        paymentCancellationReason.getAddtlInf().getFirst());

    final var paymentTypeInformation = paymentTransaction.getOrgnlTxRef().getPmtTpInf();
    assertEquals(ExternalServiceLevel1CodeDto.SRTP,
        paymentTypeInformation.getSvcLvl().getCd());
    assertEquals("PAGOPA",
        paymentTypeInformation.getLclInstrm().getPrtry());

    final var branchAndFinancialInstitutionIdentification = paymentTransaction.getOrgnlTxRef().getCdtrAgt();
    assertEquals(pagoPaConfigProperties.anag().fiscalCode(),
        branchAndFinancialInstitutionIdentification.getFinInstnId().getOthr().getId());
    assertEquals("BOID",
        branchAndFinancialInstitutionIdentification.getFinInstnId().getOthr().getSchmeNm().getCd());

    final var caseAssignment = result.getDocument().getCstmrPmtCxlReq().getAssgnmt();
    assertEquals(resourceId.getId().toString(),
        caseAssignment.getId());
    assertEquals(savingDateTime.toString(),
        caseAssignment.getCreDtTm());
    assertEquals(pagoPaConfigProperties.anag().fiscalCode(),
        caseAssignment.getAssgnr().getPty().getId().getOrgId().getOthr().getId());
    assertEquals(ExternalOrganisationIdentification1CodeIIDto.BOID,
        caseAssignment.getAssgnr().getPty().getId().getOrgId().getOthr().getSchmeNm().getCd());
    assertEquals(serviceProviderDebtor,
        caseAssignment.getAssgne().getAgt().getFinInstnId().getBICFI());

    assertEquals(this.callbackProperties.url().cancel(), result.getCallbackUrl().toString());

  }
}
