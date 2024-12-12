package it.gov.pagopa.rtp.activator.service.rtp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.CashAccount40EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ChargeBearerType1CodeDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.CreditTransferTransaction57EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.CreditorPaymentActivationRequestV10EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.DateAndDateTime2ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.DocumentEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalPersonIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalServiceLevel1CodeDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalServiceLevel1CodeWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.FinancialInstitutionIdentification18EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.GenericFinancialIdentification1Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.GenericOrganisationIdentification1EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.GenericOrganisationIdentification1EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.GenericPersonIdentification1EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.GroupHeader105EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.IBAN2007IdentifierWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ISODateTimeWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ISODateWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.InstructionForCreditorAgent3EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.Max35TextWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.Party38ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PartyIdentification135EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PartyIdentification135EPC25922V30DS023Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PartyIdentification135EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PaymentIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PaymentInstruction42EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PaymentMethod7CodeDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PaymentTypeInformation26EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PersonIdentification13EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PersonIdentification13EPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.RemittanceInformation21EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;

@Component
public class SepaRequestToPayMapper {

        public SepaRequestToPayRequestResourceDto toRequestToPay(Rtp rtp) {

                SepaRequestToPayRequestResourceDto sepaRequestToPayRequestResourceDto = new SepaRequestToPayRequestResourceDto();

                PartyIdentification135EPC25922V30DS02Dto partyIdentification135EPC25922V30DS02Dto = new PartyIdentification135EPC25922V30DS02Dto();
                partyIdentification135EPC25922V30DS02Dto.setNm("PagoPA");// FIXED

                GroupHeader105EPC25922V30DS02Dto groupHeader105EPC25922V30DS02Dto = new GroupHeader105EPC25922V30DS02Dto();
                groupHeader105EPC25922V30DS02Dto.setMsgId(rtp.resourceID().getId().toString());

                groupHeader105EPC25922V30DS02Dto.setCreDtTm(rtp.savingDateTime().toString());
                groupHeader105EPC25922V30DS02Dto.setNbOfTxs("1");// FIXED
                groupHeader105EPC25922V30DS02Dto.setInitgPty(partyIdentification135EPC25922V30DS02Dto);

                ISODateWrapperDto isoDateWrapperDto = new ISODateWrapperDto();
                isoDateWrapperDto.setDt(rtp.expiryDate().toString());

                ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto externalPersonIdentification1CodeEPC25922V30DS02WrapperDto = new ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto();
                externalPersonIdentification1CodeEPC25922V30DS02WrapperDto
                                .cd(ExternalPersonIdentification1CodeEPC25922V30DS02Dto.POID);

                GenericPersonIdentification1EPC25922V30DS02Dto genericPersonIdentification1EPC25922V30DS02Dto = new GenericPersonIdentification1EPC25922V30DS02Dto(
                                rtp.payerId());
                genericPersonIdentification1EPC25922V30DS02Dto
                                .setSchmeNm(externalPersonIdentification1CodeEPC25922V30DS02WrapperDto);

                List<GenericPersonIdentification1EPC25922V30DS02Dto> lGenericPersonIdentification1EPC25922V30DS02Dtos = new ArrayList<>();
                lGenericPersonIdentification1EPC25922V30DS02Dtos.add(genericPersonIdentification1EPC25922V30DS02Dto);

                PersonIdentification13EPC25922V30DS02Dto personIdentification13EPC25922V30DS02Dto = new PersonIdentification13EPC25922V30DS02Dto();
                personIdentification13EPC25922V30DS02Dto.setOthr(lGenericPersonIdentification1EPC25922V30DS02Dtos);

                PersonIdentification13EPC25922V30DS02WrapperDto personIdentification13EPC25922V30DS02WrapperDto = new PersonIdentification13EPC25922V30DS02WrapperDto();
                personIdentification13EPC25922V30DS02WrapperDto.setPrvtId(personIdentification13EPC25922V30DS02Dto);

                PartyIdentification135EPC25922V30DS022Dto partyIdentification135EPC25922V30DS022Dto = new PartyIdentification135EPC25922V30DS022Dto();
                partyIdentification135EPC25922V30DS022Dto.setNm("Mario Rossi");// FIXED TO CHANGE
                partyIdentification135EPC25922V30DS022Dto
                                .setId(personIdentification13EPC25922V30DS02WrapperDto);

                FinancialInstitutionIdentification18EPC25922V30DS02Dto dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto();
                dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto.setBICFI(rtp.rtpSpId());

                BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto();
                dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto
                                .setFinInstnId(dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto);

                PaymentIdentification6EPC25922V30DS02Dto paymentIdentification6EPC25922V30DS02Dto = new PaymentIdentification6EPC25922V30DS02Dto(
                                rtp.resourceID().getId().toString(), rtp.endToEndId());

                ExternalServiceLevel1CodeWrapperDto externalServiceLevel1CodeWrapperDto = new ExternalServiceLevel1CodeWrapperDto();
                externalServiceLevel1CodeWrapperDto.setCd(ExternalServiceLevel1CodeDto.SRTP);// FIXED

                Max35TextWrapperDto max35TextWrapperDto = new Max35TextWrapperDto();
                max35TextWrapperDto.setPrtry("PAGOPA"); // FIXED

                PaymentTypeInformation26EPC25922V30DS02Dto paymentTypeInformation26EPC25922V30DS02Dto = new PaymentTypeInformation26EPC25922V30DS02Dto(
                                externalServiceLevel1CodeWrapperDto, max35TextWrapperDto);

                ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto activeOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto = new ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto();
                activeOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto
                                .setInstdAmt(rtp.amount());

                Max35TextWrapperDto cdtMax35TextWrapperDto = new Max35TextWrapperDto();
                cdtMax35TextWrapperDto.setPrtry("LEI");// FIXED

                GenericFinancialIdentification1Dto genericFinancialIdentification1Dto = new GenericFinancialIdentification1Dto();
                genericFinancialIdentification1Dto.setId("15376371009");// FIXED PAGOPA FC
                genericFinancialIdentification1Dto.setSchmeNm(cdtMax35TextWrapperDto);

                FinancialInstitutionIdentification18EPC25922V30DS02Dto financialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto();
                financialInstitutionIdentification18EPC25922V30DS02Dto.setOthr(genericFinancialIdentification1Dto);

                BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto();
                cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto
                                .setFinInstnId(financialInstitutionIdentification18EPC25922V30DS02Dto);

                ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto externalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto = new ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto();
                externalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto.setCd(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID);

                GenericOrganisationIdentification1EPC25922V30DS022Dto genericOrganisationIdentification1EPC25922V30DS022Dto = new GenericOrganisationIdentification1EPC25922V30DS022Dto();
                genericOrganisationIdentification1EPC25922V30DS022Dto.setId(rtp.payeeId());
                genericOrganisationIdentification1EPC25922V30DS022Dto.setSchmeNm(externalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto);

                List<GenericOrganisationIdentification1EPC25922V30DS022Dto> lGenericOrganisationIdentification1EPC25922V30DS022Dtos = new ArrayList<>();
                lGenericOrganisationIdentification1EPC25922V30DS022Dtos.add(genericOrganisationIdentification1EPC25922V30DS022Dto);

                OrganisationIdentification29EPC25922V30DS022Dto organisationIdentification29EPC25922V30DS022Dto = new OrganisationIdentification29EPC25922V30DS022Dto();
                
                organisationIdentification29EPC25922V30DS022Dto.setOthr(lGenericOrganisationIdentification1EPC25922V30DS022Dtos);

                OrganisationIdentification29EPC25922V30DS022WrapperDto organisationIdentification29EPC25922V30DS022WrapperDto = new OrganisationIdentification29EPC25922V30DS022WrapperDto();
                organisationIdentification29EPC25922V30DS022WrapperDto.setOrgId(organisationIdentification29EPC25922V30DS022Dto);
                
                PartyIdentification135EPC25922V30DS023Dto partyIdentification135EPC25922V30DS023Dto = new PartyIdentification135EPC25922V30DS023Dto();
                partyIdentification135EPC25922V30DS023Dto.setNm(rtp.payeeName());
                partyIdentification135EPC25922V30DS023Dto
                                .setId(organisationIdentification29EPC25922V30DS022WrapperDto);

                IBAN2007IdentifierWrapperDto iban2007IdentifierWrapperDto = new IBAN2007IdentifierWrapperDto();
                iban2007IdentifierWrapperDto.setIBAN(rtp.iban());

                CashAccount40EPC25922V30DS022Dto cashAccount40EPC25922V30DS022Dto = new CashAccount40EPC25922V30DS022Dto();
                cashAccount40EPC25922V30DS022Dto.setId(iban2007IdentifierWrapperDto);

                InstructionForCreditorAgent3EPC25922V30DS02Dto payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto(
                                rtp.payTrxRef());
                InstructionForCreditorAgent3EPC25922V30DS02Dto flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto(
                                rtp.flgConf());

                List<InstructionForCreditorAgent3EPC25922V30DS02Dto> lInstructionForCreditorAgent3EPC25922V30DS02Dtos = new ArrayList<>();
                lInstructionForCreditorAgent3EPC25922V30DS02Dtos
                                .add(payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto);
                lInstructionForCreditorAgent3EPC25922V30DS02Dtos
                                .add(flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto);

                List<String> lUstrd = new ArrayList<>();
                lUstrd.add("TARI immobile 1234/BU-2024-23231312 -");// FIXED VALUE TO CHANGE
                lUstrd.add(rtp.description());

                RemittanceInformation21EPC25922V30DS02Dto remittanceInformation21EPC25922V30DS02Dto = new RemittanceInformation21EPC25922V30DS02Dto();
                remittanceInformation21EPC25922V30DS02Dto.setUstrd(lUstrd);

                CreditTransferTransaction57EPC25922V30DS02Dto creditTransferTransaction57EPC25922V30DS02Dto = new CreditTransferTransaction57EPC25922V30DS02Dto();
                creditTransferTransaction57EPC25922V30DS02Dto.setPmtId(paymentIdentification6EPC25922V30DS02Dto);
                creditTransferTransaction57EPC25922V30DS02Dto.setPmtTpInf(paymentTypeInformation26EPC25922V30DS02Dto);
                creditTransferTransaction57EPC25922V30DS02Dto
                                .setAmt(activeOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto);
                creditTransferTransaction57EPC25922V30DS02Dto.setChrgBr(ChargeBearerType1CodeDto.SLEV);// FIXED
                creditTransferTransaction57EPC25922V30DS02Dto
                                .setCdtrAgt(cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto);
                creditTransferTransaction57EPC25922V30DS02Dto.setCdtr(partyIdentification135EPC25922V30DS023Dto);
                creditTransferTransaction57EPC25922V30DS02Dto.setCdtrAcct(cashAccount40EPC25922V30DS022Dto);
                creditTransferTransaction57EPC25922V30DS02Dto
                                .instrForCdtrAgt(lInstructionForCreditorAgent3EPC25922V30DS02Dtos);
                creditTransferTransaction57EPC25922V30DS02Dto.setRmtInf(remittanceInformation21EPC25922V30DS02Dto);

                List<CreditTransferTransaction57EPC25922V30DS02Dto> lCreditTransferTransaction57EPC25922V30DS02Dtos = new ArrayList<>();
                lCreditTransferTransaction57EPC25922V30DS02Dtos.add(creditTransferTransaction57EPC25922V30DS02Dto);

                PaymentInstruction42EPC25922V30DS02Dto paymentInstruction42EPC25922V30DS02Dto = new PaymentInstruction42EPC25922V30DS02Dto();
                paymentInstruction42EPC25922V30DS02Dto.setPmtInfId(rtp.noticeNumber());
                paymentInstruction42EPC25922V30DS02Dto.setPmtMtd(PaymentMethod7CodeDto.TRF);// FIXED
                paymentInstruction42EPC25922V30DS02Dto.setReqdExctnDt(isoDateWrapperDto);
                paymentInstruction42EPC25922V30DS02Dto.setXpryDt(isoDateWrapperDto);
                paymentInstruction42EPC25922V30DS02Dto.setDbtr(partyIdentification135EPC25922V30DS022Dto);
                paymentInstruction42EPC25922V30DS02Dto
                                .setDbtrAgt(dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto);
                paymentInstruction42EPC25922V30DS02Dto.setCdtTrfTx(lCreditTransferTransaction57EPC25922V30DS02Dtos);

                List<PaymentInstruction42EPC25922V30DS02Dto> listPaymentInstruction42EPC25922V30DS02Dto = new ArrayList<>();
                listPaymentInstruction42EPC25922V30DS02Dto.add(paymentInstruction42EPC25922V30DS02Dto);

                CreditorPaymentActivationRequestV10EPC25922V30DS02Dto creditorPaymentActivationRequestV10EPC25922V30DS02Dto = new CreditorPaymentActivationRequestV10EPC25922V30DS02Dto();
                creditorPaymentActivationRequestV10EPC25922V30DS02Dto.setGrpHdr(groupHeader105EPC25922V30DS02Dto);
                creditorPaymentActivationRequestV10EPC25922V30DS02Dto
                                .setPmtInf(listPaymentInstruction42EPC25922V30DS02Dto);

                DocumentEPC25922V30DS02Dto documentEPC25922V30DS02Dto = new DocumentEPC25922V30DS02Dto();
                documentEPC25922V30DS02Dto.setCdtrPmtActvtnReq(creditorPaymentActivationRequestV10EPC25922V30DS02Dto);

                sepaRequestToPayRequestResourceDto.setCallbackUrl(URI.create("http://spsrtp.api.cstar.pagopa.it"));// FIXED
                sepaRequestToPayRequestResourceDto.setResourceId(rtp.resourceID().getId().toString());
                sepaRequestToPayRequestResourceDto.document(documentEPC25922V30DS02Dto);

                return sepaRequestToPayRequestResourceDto;
        }
}
