package it.gov.pagopa.rtp.activator.controller.activation;


import static org.assertj.core.api.Assertions.assertThat;

import it.gov.pagopa.rtp.activator.domain.payer.ActivationID;
import it.gov.pagopa.rtp.activator.domain.payer.Payer;
import it.gov.pagopa.rtp.activator.model.generated.activate.ActivationDto;
import it.gov.pagopa.rtp.activator.model.generated.activate.PageOfActivationsDto;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ActivationDtoMapperTest {

  public static final String FISCAL_CODE = "fiscalCode";
  public static final String SERVICE_PROVIDER_DEBTOR = "serviceProviderDebtor";
  public static final UUID ID = UUID.randomUUID();
  public static final Instant NOW = Instant.now();

  private final ActivationDtoMapper mapper = new ActivationDtoMapper();

  @Test
  void testToActivationDtoFromPayer() {
    Payer payer = new Payer(new ActivationID(ID), SERVICE_PROVIDER_DEBTOR, FISCAL_CODE, NOW);

    ActivationDto dto = mapper.toActivationDto(payer);

    assertThat(dto.getId()).isEqualTo(ID);
    assertThat(dto.getPayer().getFiscalCode()).isEqualTo(FISCAL_CODE);
    assertThat(dto.getPayer().getRtpSpId()).isEqualTo(SERVICE_PROVIDER_DEBTOR);
    assertThat(dto.getEffectiveActivationDate()).isEqualTo(LocalDateTime.ofInstant(NOW, ZoneOffset.UTC));
  }

  @Test
  void testToPageDto() {
    Payer payer = new Payer(new ActivationID(ID), SERVICE_PROVIDER_DEBTOR, FISCAL_CODE, NOW);

    List<Payer> list = List.of(payer);
    long totalElements = 25;
    int page = 1;
    int size = 10;

    PageOfActivationsDto pageDto = mapper.toPageDto(list, totalElements, page, size);

    assertThat(pageDto.getActivations()).hasSize(1);
    assertThat(pageDto.getPage().getTotalElements()).isEqualTo(totalElements);
    assertThat(pageDto.getPage().getTotalPages()).isEqualTo(3L);
    assertThat(pageDto.getPage().getPage()).isEqualTo(page);
    assertThat(pageDto.getPage().getSize()).isEqualTo(size);
  }
}
