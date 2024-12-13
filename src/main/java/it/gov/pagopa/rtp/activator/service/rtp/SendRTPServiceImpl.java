package it.gov.pagopa.rtp.activator.service.rtp;

import it.gov.pagopa.rtp.activator.domain.rtp.ResourceID;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.model.generated.epc.ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ExternalServiceLevel1CodeWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.IBAN2007IdentifierWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.ISODateWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.Max35TextWrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.OrganisationIdentification29EPC25922V30DS022WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.PersonIdentification13EPC25922V30DS02WrapperDto;
import it.gov.pagopa.rtp.activator.model.generated.epc.SepaRequestToPayRequestResourceDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Service
@Slf4j
@RegisterReflectionForBinding({ SepaRequestToPayRequestResourceDto.class,
    PersonIdentification13EPC25922V30DS02WrapperDto.class, ISODateWrapperDto.class,
    ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto.class, ExternalServiceLevel1CodeWrapperDto.class,
    ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto.class, Max35TextWrapperDto.class,
    OrganisationIdentification29EPC25922V30DS022WrapperDto.class,
    ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto.class, IBAN2007IdentifierWrapperDto.class
})
public class SendRTPServiceImpl implements SendRTPService {

  private SepaRequestToPayMapper sepaRequestToPayMapper;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
  }

  @Override
  public Mono<Rtp> send(Rtp rtp) {

    // save
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    try { //
      String jsonString = objectMapper.writeValueAsString(sepaRequestToPayMapper.toRequestToPay(rtp));
      log.info(jsonString);
    } catch (JsonProcessingException e) {
      log.error("Problem while serializing SepaRequestToPayRequestResourceDto object", e);
    }

    return Mono.just(rtp);
  }
}