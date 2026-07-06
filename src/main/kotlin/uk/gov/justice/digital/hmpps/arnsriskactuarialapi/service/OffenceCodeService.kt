package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient.ManageOffencesApiRestClient

@Service
class OffenceCodeService(
  private val manageOffencesClient: ManageOffencesApiRestClient,
  private val cacheService: OffenceCodeCacheService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun updateOffenceCodeMappings() {
    log.info("Updating offence code mappings...")

    val offenceCodeMappings = manageOffencesClient.getActuarialMapping()

    if (offenceCodeMappings.isEmpty()) {
      throw IllegalStateException("Received offence code map containing no entries from Manage Offences API.")
    }

    cacheService.sync(offenceCodeMappings)
  }
}
