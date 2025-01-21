package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.epcClient.model.AccountIdentification4ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.AmountType4ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CashAccount40EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ChargeBearerType1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditTransferTransaction57EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.CreditorPaymentActivationRequestV10EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.DateAndDateTime2ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.DocumentEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalPersonIdentification1CodeEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.ExternalServiceLevel1CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.FinancialIdentificationSchemeName1ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.FinancialInstitutionIdentification18EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericFinancialIdentification1Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericOrganisationIdentification1EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GenericPersonIdentification1EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.GroupHeader105EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.InstructionForCreditorAgent3EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.LocalInstrument2ChoiceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentification29EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.Party38ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS022Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS023Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PartyIdentification135EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentIdentification6EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentInstruction42EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentMethod7CodeDto;
import it.gov.pagopa.rtp.activator.epcClient.model.PaymentTypeInformation26EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PersonIdentification13EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.PersonIdentificationSchemeName1ChoiceEPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.RemittanceInformation21EPC25922V30DS02Dto;
import it.gov.pagopa.rtp.activator.epcClient.model.SepaRequestToPayRequestResourceDto;
import it.gov.pagopa.rtp.activator.epcClient.model.ServiceLevel8ChoiceDto;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

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

                var dateAndDateTime2ChoiceEPC25922V30DS02Dto = new DateAndDateTime2ChoiceEPC25922V30DS02Dto()
                    .dt(rtp.expiryDate().toString());
//                ISODateWrapperDto isoDateWrapperDto = new ISODateWrapperDto();
//                isoDateWrapperDto.setDt(rtp.expiryDate().toString());

                var personIdentificationSchemeName1ChoiceEPC25922V30DS02Dto = new PersonIdentificationSchemeName1ChoiceEPC25922V30DS02Dto()
                    .cd(ExternalPersonIdentification1CodeEPC25922V30DS02Dto.POID);
//                ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto externalPersonIdentification1CodeEPC25922V30DS02WrapperDto = new ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto();
//                externalPersonIdentification1CodeEPC25922V30DS02WrapperDto
//                                .cd(ExternalPersonIdentification1CodeEPC25922V30DS02Dto.POID);

                var genericPersonIdentification1EPC25922V30DS02Dto = new GenericPersonIdentification1EPC25922V30DS02Dto()
                    .id(rtp.payerId())
                    .schmeNm(personIdentificationSchemeName1ChoiceEPC25922V30DS02Dto);
//                genericPersonIdentification1EPC25922V30DS02Dto
//                                .setSchmeNm(personIdentificationSchemeName1ChoiceEPC25922V30DS02Dto);

                List<GenericPersonIdentification1EPC25922V30DS02Dto> lGenericPersonIdentification1EPC25922V30DS02Dtos = new ArrayList<>();
                lGenericPersonIdentification1EPC25922V30DS02Dtos.add(genericPersonIdentification1EPC25922V30DS02Dto);

                PersonIdentification13EPC25922V30DS02Dto personIdentification13EPC25922V30DS02Dto = new PersonIdentification13EPC25922V30DS02Dto();
                personIdentification13EPC25922V30DS02Dto.setOthr(lGenericPersonIdentification1EPC25922V30DS02Dtos);

                var party38ChoiceEPC25922V30DS02Dto = new Party38ChoiceEPC25922V30DS02Dto()
                    .prvtId(personIdentification13EPC25922V30DS02Dto);
//                party38ChoiceEPC25922V30DS02Dto.setPrvtId(personIdentification13EPC25922V30DS02Dto);
//                PersonIdentification13EPC25922V30DS02WrapperDto personIdentification13EPC25922V30DS02WrapperDto = new PersonIdentification13EPC25922V30DS02WrapperDto();
//                personIdentification13EPC25922V30DS02WrapperDto.setPrvtId(personIdentification13EPC25922V30DS02Dto);

                var partyIdentification135EPC25922V30DS022Dto = new PartyIdentification135EPC25922V30DS022Dto()
                    .nm(rtp.payerName())
                    .id(party38ChoiceEPC25922V30DS02Dto);
//                partyIdentification135EPC25922V30DS022Dto.setNm(rtp.payerName());
//                partyIdentification135EPC25922V30DS022Dto.setId(party38ChoiceEPC25922V30DS02Dto);

                FinancialInstitutionIdentification18EPC25922V30DS02Dto dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto();
                dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto.setBICFI(rtp.rtpSpId());

                BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto();
                dbtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto
                                .setFinInstnId(dbtFinancialInstitutionIdentification18EPC25922V30DS02Dto);

                var paymentIdentification6EPC25922V30DS02Dto = new PaymentIdentification6EPC25922V30DS02Dto()
                    .instrId(rtp.resourceID().getId().toString()).endToEndId(rtp.noticeNumber());
//                                rtp.resourceID().getId().toString(), rtp.noticeNumber());

                var serviceLevel8ChoiceDto = new ServiceLevel8ChoiceDto()
                    .cd(ExternalServiceLevel1CodeDto.SRTP);
//                ExternalServiceLevel1CodeWrapperDto externalServiceLevel1CodeWrapperDto = new ExternalServiceLevel1CodeWrapperDto();
//                externalServiceLevel1CodeWrapperDto.setCd(ExternalServiceLevel1CodeDto.SRTP);// FIXED

                var localInstrument2ChoiceDto = new LocalInstrument2ChoiceDto()
                    .prtry("PAGOPA");// FIXED
//                Max35TextWrapperDto max35TextWrapperDto = new Max35TextWrapperDto();
//                max35TextWrapperDto.setPrtry("PAGOPA"); // FIXED

                var paymentTypeInformation26EPC25922V30DS02Dto = new PaymentTypeInformation26EPC25922V30DS02Dto()
                    .svcLvl(serviceLevel8ChoiceDto)
                    .lclInstrm(localInstrument2ChoiceDto);
//                                externalServiceLevel1CodeWrapperDto, max35TextWrapperDto);

                var amountType4ChoiceEPC25922V30DS02Dto = new AmountType4ChoiceEPC25922V30DS02Dto()
                    .instdAmt(rtp.amount().movePointLeft(2));
//                ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto activeOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto = new ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto();
//                activeOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto
//                                .setInstdAmt(rtp.amount().movePointLeft(2));

                var financialIdentificationSchemeName1ChoiceDto = new FinancialIdentificationSchemeName1ChoiceDto()
                    .prtry("LEI"); // FIXED
//                Max35TextWrapperDto cdtMax35TextWrapperDto = new Max35TextWrapperDto();
//                cdtMax35TextWrapperDto.setPrtry("LEI");// FIXED

                var genericFinancialIdentification1Dto = new GenericFinancialIdentification1Dto()
                    .id("15376371009") // FIXED PAGOPA FC
                    .schmeNm(financialIdentificationSchemeName1ChoiceDto);
//                genericFinancialIdentification1Dto.setId("15376371009");// FIXED PAGOPA FC
//                genericFinancialIdentification1Dto.setSchmeNm(financialIdentificationSchemeName1ChoiceDto);

                FinancialInstitutionIdentification18EPC25922V30DS02Dto financialInstitutionIdentification18EPC25922V30DS02Dto = new FinancialInstitutionIdentification18EPC25922V30DS02Dto();
                financialInstitutionIdentification18EPC25922V30DS02Dto.setOthr(genericFinancialIdentification1Dto);

                BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto = new BranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto();
                cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto
                                .setFinInstnId(financialInstitutionIdentification18EPC25922V30DS02Dto);

                var organisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto = new OrganisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto()
                    .cd(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID);
//                ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto externalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto = new ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto();
//                externalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto
//                                .setCd(ExternalOrganisationIdentification1CodeEPC25922V30DS022Dto.BOID);

                var genericOrganisationIdentification1EPC25922V30DS022Dto = new GenericOrganisationIdentification1EPC25922V30DS022Dto()
                    .id(rtp.payeeId())
                    .schmeNm(organisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto);
//                genericOrganisationIdentification1EPC25922V30DS022Dto.setId(rtp.payeeId());
//                genericOrganisationIdentification1EPC25922V30DS022Dto
//                                .setSchmeNm(organisationIdentificationSchemeName1ChoiceEPC25922V30DS022Dto);

                List<GenericOrganisationIdentification1EPC25922V30DS022Dto> lGenericOrganisationIdentification1EPC25922V30DS022Dtos = new ArrayList<>();
                lGenericOrganisationIdentification1EPC25922V30DS022Dtos
                                .add(genericOrganisationIdentification1EPC25922V30DS022Dto);

                OrganisationIdentification29EPC25922V30DS022Dto organisationIdentification29EPC25922V30DS022Dto = new OrganisationIdentification29EPC25922V30DS022Dto();

                organisationIdentification29EPC25922V30DS022Dto
                                .setOthr(lGenericOrganisationIdentification1EPC25922V30DS022Dtos);

                var party38ChoiceEPC25922V30DS022Dto = new Party38ChoiceEPC25922V30DS022Dto()
                    .orgId(organisationIdentification29EPC25922V30DS022Dto);
//                OrganisationIdentification29EPC25922V30DS022WrapperDto organisationIdentification29EPC25922V30DS022WrapperDto = new OrganisationIdentification29EPC25922V30DS022WrapperDto();
//                organisationIdentification29EPC25922V30DS022WrapperDto
//                                .setOrgId(organisationIdentification29EPC25922V30DS022Dto);

                var partyIdentification135EPC25922V30DS023Dto = new PartyIdentification135EPC25922V30DS023Dto()
                    .nm(rtp.payeeName())
                    .id(party38ChoiceEPC25922V30DS022Dto);
//                partyIdentification135EPC25922V30DS023Dto.setNm(rtp.payeeName());
//                partyIdentification135EPC25922V30DS023Dto
//                                .setId(party38ChoiceEPC25922V30DS022Dto);

                var accountIdentification4ChoiceDto = new AccountIdentification4ChoiceDto();
                accountIdentification4ChoiceDto.setIBAN(rtp.iban());
//                IBAN2007IdentifierWrapperDto iban2007IdentifierWrapperDto = new IBAN2007IdentifierWrapperDto();
//                iban2007IdentifierWrapperDto.setIBAN(rtp.iban());

                var cashAccount40EPC25922V30DS022Dto = new CashAccount40EPC25922V30DS022Dto();
                cashAccount40EPC25922V30DS022Dto.setId(accountIdentification4ChoiceDto);

                var payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto()
                    .instrInf("ATR113/" + rtp.payTrxRef());
//                                "ATR113/"+rtp.payTrxRef());
                var flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto = new InstructionForCreditorAgent3EPC25922V30DS02Dto()
                    .instrInf(rtp.flgConf());
//                                rtp.flgConf());

                List<InstructionForCreditorAgent3EPC25922V30DS02Dto> lInstructionForCreditorAgent3EPC25922V30DS02Dtos = new ArrayList<>();
                lInstructionForCreditorAgent3EPC25922V30DS02Dtos
                                .add(payTrxRefinstructionForCreditorAgent3EPC25922V30DS02Dto);
                lInstructionForCreditorAgent3EPC25922V30DS02Dtos
                                .add(flgConfRefinstructionForCreditorAgent3EPC25922V30DS02Dto);

                List<String> lUstrd = new ArrayList<>();
                lUstrd.add(rtp.subject() + "/" + rtp.noticeNumber() + " -");
                lUstrd.add("ATS001/" + rtp.description());

                RemittanceInformation21EPC25922V30DS02Dto remittanceInformation21EPC25922V30DS02Dto = new RemittanceInformation21EPC25922V30DS02Dto();
                remittanceInformation21EPC25922V30DS02Dto.setUstrd(lUstrd);

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
//                creditTransferTransaction57EPC25922V30DS02Dto.setPmtId(paymentIdentification6EPC25922V30DS02Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto.setPmtTpInf(paymentTypeInformation26EPC25922V30DS02Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto
//                                .setAmt(amountType4ChoiceEPC25922V30DS02Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto.setChrgBr(ChargeBearerType1CodeDto.SLEV);// FIXED
//                creditTransferTransaction57EPC25922V30DS02Dto
//                                .setCdtrAgt(cdtBranchAndFinancialInstitutionIdentification6EPC25922V30DS02Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto.setCdtr(partyIdentification135EPC25922V30DS023Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto.setCdtrAcct(cashAccount40EPC25922V30DS022Dto);
//                creditTransferTransaction57EPC25922V30DS02Dto
//                                .instrForCdtrAgt(lInstructionForCreditorAgent3EPC25922V30DS02Dtos);
//                creditTransferTransaction57EPC25922V30DS02Dto.setRmtInf(remittanceInformation21EPC25922V30DS02Dto);

                List<CreditTransferTransaction57EPC25922V30DS02Dto> lCreditTransferTransaction57EPC25922V30DS02Dtos = new ArrayList<>();
                lCreditTransferTransaction57EPC25922V30DS02Dtos.add(creditTransferTransaction57EPC25922V30DS02Dto);

                PaymentInstruction42EPC25922V30DS02Dto paymentInstruction42EPC25922V30DS02Dto = new PaymentInstruction42EPC25922V30DS02Dto();
                paymentInstruction42EPC25922V30DS02Dto.setPmtInfId(rtp.noticeNumber());
                paymentInstruction42EPC25922V30DS02Dto.setPmtMtd(PaymentMethod7CodeDto.TRF);// FIXED
                paymentInstruction42EPC25922V30DS02Dto.setReqdExctnDt(dateAndDateTime2ChoiceEPC25922V30DS02Dto);
                paymentInstruction42EPC25922V30DS02Dto.setXpryDt(dateAndDateTime2ChoiceEPC25922V30DS02Dto);
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
