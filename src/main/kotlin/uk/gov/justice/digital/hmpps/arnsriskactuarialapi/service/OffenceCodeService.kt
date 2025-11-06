package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.restclient.ManageOffencesApiRestClient

@Service
class OffenceCodeService(
  private val manageOffencesClient: ManageOffencesApiRestClient,
  private val cacheService: OffenceCodeCacheService,
) {

  private val log = LoggerFactory.getLogger(this::class.java)

  fun updateOffenceCodeMappings() {
    log.info("Updating offence code mappings...")

    val hoCodes = manageOffencesClient.getActuarialMapping()

    if (hoCodes.isEmpty()) {
      throw IllegalStateException("Received empty Actuarial Mapping list from Manage Offences API.")
    }

    val offenceCodeMappings = transformHoCodesToOffenceCodeMappings(hoCodes)

    cacheService.sync(offenceCodeMappings)
  }

  private fun transformHoCodesToOffenceCodeMappings(hoCodes: List<HoCode>): Map<String, OffenceCodeValues> {
    return hoCodes.associate { hoCode ->
      val offenceKey = "%03d%02d".format(hoCode.category, hoCode.subCategory)

      val weightings = hoCode.weightings.associateBy { it.name }
      val flags = hoCode.flags.associateBy { it.name }

      offenceKey to OffenceCodeValues(
        ogrs3Weighting = weightings["ogrs3Weighting"]?.value,
        snsvStaticWeighting = weightings["snsvStaticWeighting"]?.value,
        snsvDynamicWeighting = weightings["snsvDynamicWeighting"]?.value,
        snsvVatpStaticWeighting = weightings["snsvVatpStaticWeighting"]?.value,
        snsvVatpDynamicWeighting = weightings["snsvVatpDynamicWeighting"]?.value,
        opdViolenceSexFlag = flags["opdViolSex"]?.value
      )
    }
  }
}