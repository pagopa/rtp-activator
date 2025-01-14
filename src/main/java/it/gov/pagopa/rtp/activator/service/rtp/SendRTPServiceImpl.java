package it.gov.pagopa.rtp.activator.service.rtp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.gov.pagopa.rtp.activator.activateClient.api.ReadApi;
import it.gov.pagopa.rtp.activator.activateClient.model.ActivationDto;
import it.gov.pagopa.rtp.activator.configuration.ServiceProviderConfig;
import it.gov.pagopa.rtp.activator.domain.errors.PayerNotActivatedException;
import it.gov.pagopa.rtp.activator.domain.rtp.Rtp;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpRepository;
import it.gov.pagopa.rtp.activator.domain.rtp.RtpStatus;
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
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RegisterReflectionForBinding({SepaRequestToPayRequestResourceDto.class,
    PersonIdentification13EPC25922V30DS02WrapperDto.class, ISODateWrapperDto.class,
    ExternalPersonIdentification1CodeEPC25922V30DS02WrapperDto.class,
    ExternalServiceLevel1CodeWrapperDto.class,
    ActiveOrHistoricCurrencyAndAmountEPC25922V30DS02WrapperDto.class, Max35TextWrapperDto.class,
    OrganisationIdentification29EPC25922V30DS022WrapperDto.class,
    ExternalOrganisationIdentification1CodeEPC25922V30DS022WrapperDto.class,
    IBAN2007IdentifierWrapperDto.class,
    ActivationDto.class
})
public class SendRTPServiceImpl implements SendRTPService {

  private final SepaRequestToPayMapper sepaRequestToPayMapper;
  private final ReadApi activationApi;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final ServiceProviderConfig serviceProviderConfig;
  private final RtpRepository rtpRepository;

  public SendRTPServiceImpl(SepaRequestToPayMapper sepaRequestToPayMapper, ReadApi activationApi,
      ServiceProviderConfig serviceProviderConfig, RtpRepository rtpRepository) {
    this.sepaRequestToPayMapper = sepaRequestToPayMapper;
    this.activationApi = activationApi;
    this.serviceProviderConfig = serviceProviderConfig;
    this.rtpRepository = rtpRepository;
    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
  }

  @Override
  public Mono<Rtp> send(Rtp rtp) {

    return activationApi.findActivationByPayerId(UUID.randomUUID(), rtp.payerId(),
            serviceProviderConfig.apiVersion())
        .map(act -> toRtpWithActivationInfo(rtp, act))
        .flatMap(rtpDto -> {
          log.info(rtpToJson(rtpDto));
          return Mono.just(rtpDto);
        })
        .flatMap(rtpRepository::save)
        .onErrorMap(WebClientResponseException.class, mapResponseToException())
        .switchIfEmpty(Mono.error(new PayerNotActivatedException()));
  }

  private Rtp toRtpWithActivationInfo(Rtp rtp, ActivationDto activationDto) {
    return Rtp.builder()
        .rtpSpId(activationDto.getPayer().getRtpSpId())
        .endToEndId(rtp.endToEndId())
        .iban(rtp.iban())
        .payTrxRef(rtp.payTrxRef())
        .flgConf(rtp.flgConf())
        .payerId(rtp.payerId())
        .payeeName(rtp.payeeName())
        .payeeId(rtp.payeeId())
        .noticeNumber(rtp.noticeNumber())
        .amount(rtp.amount())
        .description(rtp.description())
        .expiryDate(rtp.expiryDate())
        .resourceID(rtp.resourceID())
        .savingDateTime(rtp.savingDateTime())
        .status(RtpStatus.CREATED)
        .build();
  }

  private String rtpToJson(Rtp rtpToLog) {
    String jsonString = "";
    try {
      jsonString = objectMapper.writeValueAsString(
          sepaRequestToPayMapper.toRequestToPay(rtpToLog));
    } catch (JsonProcessingException e) {
      log.error("Problem while serializing SepaRequestToPayRequestResourceDto object", e);
    }
    return jsonString;
  }

  private Function<WebClientResponseException, Throwable> mapResponseToException() {
    return exception -> {
      if (exception.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
        return new PayerNotActivatedException();
      }
      return new RuntimeException("Internal Server Error");
    };
  }
}