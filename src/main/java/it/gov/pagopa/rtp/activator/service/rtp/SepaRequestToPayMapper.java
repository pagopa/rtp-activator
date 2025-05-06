package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.configuration.CallbackProperties;
import it.gov.pagopa.rtp.activator.configuration.PagoPaConfigProperties;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.epcClient.model.AccountIdentification4ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.AmountType4ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CancellationReason33ChoiceEPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CaseAssignment5EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CashAccount38Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CashAccount40EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ChargeBearerType1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditTransferTransaction57EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditorPaymentActivationRequestV10EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CustomerPaymentCancellationRequestV08EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.DateAndDateTime2ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.DocumentEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.DocumentEPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalCancellationReason1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeIIDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalPersonIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalServiceLevel1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.FinancialIdentificationSchemeName1ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.FinancialInstitutionIdentification18EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericFinancialIdentification1Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericOrganisationIdentification1Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericOrganisationIdentification1EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericOrganisationIdentification1EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericOrganisationIdentification1EPC25922V30DS112Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericPersonIdentification1EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GroupHeader105EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.InstructionForCreditorAgent3EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.LocalInstrument2ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS112Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentificationSchemeName1ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS04b2Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OriginalGroupInformation29EPC25922V30DS15RTPDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OriginalPaymentInstruction34EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OriginalTransactionReference28EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS113Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party40ChoiceEPC25922V30DS113Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party40ChoiceEPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS023Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS113Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentCancellationReason5EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentInstruction42EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentMethod7CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentTransaction109EPC25922V30DS11Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentTypeInformation26EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentTypeInformation27EPC25922V30DS15RTPDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PersonIdentification13EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PersonIdentificationSchemeName1ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.RemittanceInformation16EPC25922V30DS04bDto;
import it.gov.pagopa.rtp.activator.epcClient.model.RemittanceInformation21EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayCancellationRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ServiceLevel8ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.UnderlyingTransaction24EPC25922V30DS11Dto;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import it.gov.pagopa.rtp.activator.utils.DateUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class SepaRequestToPayMapper {

  private static final String BIC_REGEX = "^([A-Z0-9]{4}[A-Z]{2}[A-Z0-9]{2}([A-Z0-9]{3})?)$";

  private final CallbackProperties callbackProperties;
  private final PagoPaConfigProperties pagoPaConfigProperties;

  public SepaRequestToPayMapper(
      @NonNull final CallbackProperties callbackProperties,
      @NonNull final PagoPaConfigProperties pagoPaConfigProperties) {

    this.callbackProperties = Objects.requireNonNull(callbackProperties,
        "Callback properties cannot be null");
    this.pagoPaConfigProperties = Objects.requireNonNull(pagoPaConfigProperties,
        "PagoPa config properties cannot be null");
  }


  public SepaRequestToPayRequestResourceDto toEpcRequestToPay(Rtp rtp) {

    var sepaRequestToPayRequestResourceDto = new SepaRequestToPayRequestResourceDto();

    var organisationIdentificationSchemeName1ChoiceDto = new OrganisationIdentificationSchemeName1ChoiceDto()
        .cd("BOID");

    var genericOrganisationIdentification1Dto = new GenericOrganisationIdentification1Dto()
        .id(this.pagoPaConfigProperties.details().fiscalCode())
        .schmeNm(organisationIdentificationSchemeName1ChoiceDto);

    var  lGenericOrganisationIdentification1Dtos = new  ArrayList<GenericOrganisationIdentification1Dto>();
    lGenericOrganisationIdentification1Dtos.add(genericOrganisationIdentification1Dto);

    var  organisationIdentification29Dto = new OrganisationIdentification29Dto()
        .othr(lGenericOrganisationIdentification1Dtos);

    var party38ChoiceDto = new Party38ChoiceDto()
        .orgId(organisationIdentification29Dto);

    var partyIdentification135EPC25922V30DS02Dto = new PartyIdentification135EPC25922V30DS02Dto()
        .nm("PagoPA")
        .id(party38ChoiceDto);

    var groupHeader105EPC25922V30DS02Dto = new GroupHeader105EPC25922V30DS02Dto()
        .msgId(rtp.resourceID().getId().toString().replace("-",""))
        .creDtTm(DateUtils.localDateTimeToOffsetFormat(rtp.savingDateTime()))
        .nbOfTxs("1")// FIXED
        .initgPty(partyIdentification135EPC25922V30DS02Dto);

    var dateAndDateTime2ChoiceEPC25922V30DS02Dto = new DateAndDateTime2ChoiceEPC25922V30DS02Dto()
        .dt(rtp.expiryDate().toString());

    var personIdentificationSchemeName1ChoiceEPC25922V30DS02Dto = new PersonIdentificationSchemeName1ChoiceEPC25922V30DS02Dto()
        .cd(ExternalPersonIdentification1CodeEPC25922V30DS02Dto.POID);

    var genericPersonIdentification1EPC25922V30DS02Dto = new GenericPersonIdentification1EPC25922V30DS02Dto()
        .id(rtp.payerId())
        .schmeNm(personIdentificationSchemeName1ChoiceEPC25922V30DS02Dto);

    var lGenericPersonIdentification1EPC25922V30DS02Dtos = new ArrayList<GenericPersonIdentification1EPC25922V30DS02Dto>();
    lGenericPersonIdentification1EPC25922V30DS02Dtos.add(
        genericPersonIdentification1EPC25922V30DS02Dto);

    var personIdentification13EPC25922V30DS02Dto = new PersonIdentification13EPC25922V30DS02Dto()
        .othr(lGenericPersonIdentification1EPC25922V30DS02Dtos);

    var party38ChoiceEPC25922V30DS02Dto = new Party38ChoiceEPC25922V30DS02Dto()
        .prvtId(personIdentification13EPC25922V30DS02Dto);

    var partyIdentification135EPC25922V30DS022Dto = new PartyIdentification135EPC25922V30DS022Dto()
        .nm(rtp.payerName())
        .id(party38ChoiceEPC25922V30DS02Dto);

    var dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto();
    if (rtp.serviceProviderDebtor().matches(BIC_REGEX)) {
      dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto.setBICFI(
          rtp.serviceProviderDebtor());
    } else {
      var financialIdentificationSchemeName1ChoiceDto = new FinancialIdentificationSchemeName1ChoiceDto();
      financialIdentificationSchemeName1ChoiceDto.setCd("BOID");

      var genericFinancialIdentification1Dto = new GenericFinancialIdentification1Dto();
      genericFinancialIdentification1Dto.setId(rtp.serviceProviderDebtor());
      genericFinancialIdentification1Dto.setSchmeNm(financialIdentificationSchemeName1ChoiceDto);
      dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto.setOthr(
          genericFinancialIdentification1Dto);
    }

    var dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto()
        .finInstnId(dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto);

    var paymentIdentification6EPC25922V30DS02Dto = new PaymentIdentification6EPC25922V30DS02Dto()
        .instrId(rtp.resourceID().getId().toString().replace("-",""))
        .endToEndId(rtp.noticeNumber());

    var serviceLevel8ChoiceDto = new ServiceLevel8ChoiceDto()
        .cd(ExternalServiceLevel1CodeDto.SRTP); // FIXED

    var localInstrument2ChoiceDto = new LocalInstrument2ChoiceDto()
        .prtry("PAGOPA"); // FIXED

    var paymentTypeInformation26EPC25922V30DS02Dto = new PaymentTypeInformation26EPC25922V30DS02Dto()
        .svcLvl(serviceLevel8ChoiceDto)
        .lclInstrm(localInstrument2ChoiceDto);

    var amountType4ChoiceEPC25922V30DS02Dto = new AmountType4ChoiceEPC25922V30DS02Dto()
        .instdAmt(rtp.amount().movePointLeft(2));

    var financialIdentificationSchemeName1ChoiceDto = new FinancialIdentificationSchemeName1ChoiceDto()
        .cd("BOID");// FIXED

    var genericFinancialIdentification1Dto = new GenericFinancialIdentification1Dto()
        .id(rtp.serviceProviderCreditor())
        .schmeNm(financialIdentificationSchemeName1ChoiceDto);

    var financialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto()
        .othr(genericFinancialIdentification1Dto);

    var cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto()
        .finInstnId(financialInstitutionIdentification18EPC25922V30DS02Dto);

    var organisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto = new OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto()
        .cd(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID);

    var genericOrganisationIdentification1EPC25922V30DS022Dto = new GenericOrganisationIdentification1EPC25922V30DS022Dto()
        .id(rtp.payeeId())
        .schmeNm(organisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto);

    var lGenericOrganisationIdentification1EPC25922V30DS022Dtos = new ArrayList<GenericOrganisationIdentification1EPC25922V30DS022Dto>();
    lGenericOrganisationIdentification1EPC25922V30DS022Dtos
        .add(genericOrganisationIdentification1EPC25922V30DS022Dto);

    var organisationIdentification29EPC25922V30DS022Dto = new OrganisationIdentification29EPC25922V30DS022Dto()
        .othr(lGenericOrganisationIdentification1EPC25922V30DS022Dtos);

    var party38ChoiceEPC25922V30DS022Dto = new Party38ChoiceEPC25922V30DS022Dto()
        .orgId(organisationIdentification29EPC25922V30DS022Dto);

    var partyIdentification135EPC25922V30DS023Dto = new PartyIdentification135EPC25922V30DS023Dto()
        .nm(rtp.payeeName())
        .id(party38ChoiceEPC25922V30DS022Dto);

    var accountIdentification4ChoiceDto = new AccountIdentification4ChoiceDto();
    accountIdentification4ChoiceDto.setIBAN(this.pagoPaConfigProperties.details().iban());

    var cashAccount40EPC25922V30DS022Dto = new CashAccount40EPC25922V30DS022Dto();
    cashAccount40EPC25922V30DS022Dto.setId(accountIdentification4ChoiceDto);

    var payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto()
        .instrInf("ATR113/" + rtp.payTrxRef());
    var flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto()
        .instrInf(rtp.flgConf());

    var lInstructionForCreditorAgent3EPC25922V30DS02Dtos = new ArrayList<InstructionForCreditorAgent3EPC25922V30DS02Dto>();
    lInstructionForCreditorAgent3EPC25922V30DS02Dtos
        .add(payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto);
    lInstructionForCreditorAgent3EPC25922V30DS02Dtos
        .add(flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto);

    List<String> lUstrd = new ArrayList<>();
    lUstrd.add(rtp.subject() + "/" + rtp.noticeNumber());
    lUstrd.add("ATS001/" + rtp.description());

    var remittanceInformation21EPC25922V30DS02Dto = new RemittanceInformation21EPC25922V30DS02Dto()
        .ustrd(lUstrd);

    var creditTransferTransaction57EPC25922V30DS02Dto = new CreditTransferTransaction57EPC25922V30DS02Dto()
        .pmtId(paymentIdentification6EPC25922V30DS02Dto)
        .pmtTpInf(paymentTypeInformation26EPC25922V30DS02Dto)
        .amt(amountType4ChoiceEPC25922V30DS02Dto)
        .chrgBr(ChargeBearerType1CodeDto.SLEV) // FIXED
        .cdtrAgt(cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto)
        .cdtr(partyIdentification135EPC25922V30DS023Dto)
        .cdtrAcct(cashAccount40EPC25922V30DS022Dto)
        .instrForCdtrAgt(lInstructionForCreditorAgent3EPC25922V30DS02Dtos)
        .rmtInf(remittanceInformation21EPC25922V30DS02Dto);

    var lCreditTransferTransaction57EPC25922V30DS02Dtos = new ArrayList<CreditTransferTransaction57EPC25922V30DS02Dto>();
    lCreditTransferTransaction57EPC25922V30DS02Dtos.add(
        creditTransferTransaction57EPC25922V30DS02Dto);

    var paymentInstruction42EPC25922V30DS02Dto = new PaymentInstruction42EPC25922V30DS02Dto()
        .pmtInfId(rtp.noticeNumber())
        .pmtMtd(PaymentMethod7CodeDto.TRF) // FIXED
        .reqdExctnDt(dateAndDateTime2ChoiceEPC25922V30DS02Dto)
        .xpryDt(dateAndDateTime2ChoiceEPC25922V30DS02Dto)
        .dbtr(partyIdentification135EPC25922V30DS022Dto)
        .dbtrAgt(dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto)
        .cdtTrfTx(lCreditTransferTransaction57EPC25922V30DS02Dtos);

    List<PaymentInstruction42EPC25922V30DS02Dto> listPaymentInstruction42EPC25922V30DS02Dto = new ArrayList<>();
    listPaymentInstruction42EPC25922V30DS02Dto.add(paymentInstruction42EPC25922V30DS02Dto);

    var creditorPaymentActivationRequestV10EPC25922V30DS02Dto = new CreditorPaymentActivationRequestV10EPC25922V30DS02Dto();
    creditorPaymentActivationRequestV10EPC25922V30DS02Dto.setGrpHdr(
        groupHeader105EPC25922V30DS02Dto);
    creditorPaymentActivationRequestV10EPC25922V30DS02Dto
        .setPmtInf(listPaymentInstruction42EPC25922V30DS02Dto);

    var documentEPC25922V30DS02Dto = new DocumentEPC25922V30DS02Dto()
        .cdtrPmtActvtnReq(creditorPaymentActivationRequestV10EPC25922V30DS02Dto);

    sepaRequestToPayRequestResourceDto.setCallbackUrl(
        URI.create(this.callbackProperties.url().send()));
    sepaRequestToPayRequestResourceDto.setResourceId(rtp.resourceID().getId().toString());
    sepaRequestToPayRequestResourceDto.document(documentEPC25922V30DS02Dto);

    return sepaRequestToPayRequestResourceDto;
  }


  @NonNull
  public SepaRequestToPayCancellationRequestResourceDto toEpcRequestToCancel(
      @NonNull final Rtp rtp) {

    final var party40ChoiceAssigner = new Party40ChoiceEPC25922V30DS11Dto()  //Assgnr
        .pty(new PartyIdentification135EPC25922V30DS04bDto()
            .id(new Party38ChoiceEPC25922V30DS04bDto()
                .orgId(new OrganisationIdentification29EPC25922V30DS04bDto()
                    .othr(new GenericOrganisationIdentification1EPC25922V30DS04bDto()
                        .id(this.pagoPaConfigProperties.details().fiscalCode())
                        .schmeNm(new OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS04bDto()
                            .cd(ExternalOrganisationIdentification1CodeIIDto.BOID))))));

    final var party40ChoiceAssignee = new Party40ChoiceEPC25922V30DS11Dto()  //Assgne
        .agt(new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto()
            .finInstnId(new FinancialInstitutionIdentification18EPC25922V30DS02Dto()
                .BICFI(rtp.serviceProviderDebtor())));

    final var caseAssignment = new CaseAssignment5EPC25922V30DS11Dto() //Assgnmt
        .id(rtp.resourceID().getId().toString())
        .assgnr(party40ChoiceAssigner)
        .assgne(party40ChoiceAssignee)
        .creDtTm(rtp.savingDateTime().toString());

    final var organisationIdentification29EPC25922V30DS112Dto = new OrganisationIdentification29EPC25922V30DS112Dto() //OrgId
        .othr(new GenericOrganisationIdentification1EPC25922V30DS112Dto()
            .id(this.pagoPaConfigProperties.details().fiscalCode())
            .schmeNm(new OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS04b2Dto()
                .cd(ExternalOrganisationIdentification1CodeEPC25922V30DS02Dto.BOID)));

    final var partyIdentification135EPC25922V30DS113Dto = new PartyIdentification135EPC25922V30DS113Dto() //Orgtr
        .nm(rtp.payeeName())
        .id(new Party38ChoiceEPC25922V30DS113Dto()
            .orgId(organisationIdentification29EPC25922V30DS112Dto));

    final var paymentCancellationReason5EPC25922V30DS11Dto = List.of("ATS005/ " + rtp.expiryDate());

    final var paymentCancellationReason = new PaymentCancellationReason5EPC25922V30DS11Dto()  //CxlRsnInf
        .orgtr(partyIdentification135EPC25922V30DS113Dto)
        .rsn(new CancellationReason33ChoiceEPC25922V30DS11Dto()
            .cd(ExternalCancellationReason1CodeDto.PAID))
        .addtlInf(paymentCancellationReason5EPC25922V30DS11Dto);

    final var paymentTypeInformation27EPC25922V30DS15RTPDto = new PaymentTypeInformation27EPC25922V30DS15RTPDto()
        .svcLvl(new ServiceLevel8ChoiceDto()
            .cd(ExternalServiceLevel1CodeDto.SRTP))
        .lclInstrm(new LocalInstrument2ChoiceDto()
            .prtry("PAGOPA"));

    final var branchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto() //CdtrAgt
        .finInstnId(new FinancialInstitutionIdentification18EPC25922V30DS02Dto()
            .othr(new GenericFinancialIdentification1Dto()
                .id(this.pagoPaConfigProperties.details().fiscalCode())
                .schmeNm(new FinancialIdentificationSchemeName1ChoiceDto()
                    .cd("BOID")
                )));

    final var originalTransactionReference28EPC25922V30DS11Dto = new OriginalTransactionReference28EPC25922V30DS11Dto() //OrgnlTxRef
        .amt(new AmountType4ChoiceEPC25922V30DS02Dto()
            .instdAmt(rtp.amount()))
        .reqdExctnDt(new DateAndDateTime2ChoiceEPC25922V30DS02Dto()
            .dt(String.valueOf(rtp.expiryDate())))
        .pmtTpInf(paymentTypeInformation27EPC25922V30DS15RTPDto)
        .rmtInf(new RemittanceInformation16EPC25922V30DS04bDto()
            .ustrd(rtp.subject()))
        .dbtrAgt(new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto()
            .finInstnId(new FinancialInstitutionIdentification18EPC25922V30DS02Dto()
                .BICFI(rtp.serviceProviderDebtor())))
        .cdtrAgt(branchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto)
        .cdtr(
            new Party40ChoiceEPC25922V30DS113Dto())
        .cdtrAcct(new CashAccount38Dto()
            .id(new AccountIdentification4ChoiceDto()
                .IBAN(this.pagoPaConfigProperties.details().iban())));

    final var paymentTransaction = List.of(new PaymentTransaction109EPC25922V30DS11Dto()  //TxInf
        .cxlId(rtp.resourceID().getId().toString())
        .orgnlInstrId(UUID.randomUUID().toString())
        .orgnlEndToEndId(rtp.noticeNumber())
        .cxlRsnInf(paymentCancellationReason)
        .orgnlTxRef(originalTransactionReference28EPC25922V30DS11Dto));

    final var originalPaymentInstruction = new OriginalPaymentInstruction34EPC25922V30DS11Dto() //OrgnlPmtInfAndCxl
        .pmtCxlId(rtp.resourceID().getId().toString())
        .orgnlPmtInfId(rtp.resourceID().getId().toString())
        .orgnlGrpInf(new OriginalGroupInformation29EPC25922V30DS15RTPDto()
            .orgnlMsgId(rtp.resourceID().getId().toString().replace("-",""))
            .orgnlMsgNmId("pain.013.001.10")
            .orgnlCreDtTm(rtp.savingDateTime().toString()))
        .txInf(paymentTransaction);

    final var underlyingTransaction = new UnderlyingTransaction24EPC25922V30DS11Dto()  //Undrlyg
        .orgnlPmtInfAndCxl(List.of(originalPaymentInstruction));

    final var customerPaymentCancellationRequest = new CustomerPaymentCancellationRequestV08EPC25922V30DS11Dto() //CstmrPmtCxlReq
        .assgnmt(caseAssignment)
        .undrlyg(underlyingTransaction);

    final var document = new DocumentEPC25922V30DS11Dto()  //Document
        .cstmrPmtCxlReq(customerPaymentCancellationRequest);

    return new SepaRequestToPayCancellationRequestResourceDto()
        .resourceId(rtp.resourceID().getId().toString())
        .document(document)
        .callbackUrl(URI.create(this.callbackProperties.url().cancel()));

  }

}
