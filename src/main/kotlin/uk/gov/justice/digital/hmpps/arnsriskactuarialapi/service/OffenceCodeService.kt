package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.HoCode
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeValues
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.OffenceCodeWeighting
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

  private fun transformHoCodesToOffenceCodeMappings(hoCodes: List<HoCode>): Map<String, OffenceCodeValues> = hoCodes.associate { hoCode ->
    val offenceKey = "%03d%02d".format(hoCode.category, hoCode.subCategory)

    val weightings = hoCode.weightings.associateBy { it.name }
    val flags = hoCode.flags.associateBy { it.name }

    fun buildOffenceCodeWeighting(name: String) = weightings[name].let {
      OffenceCodeWeighting(
        value = it?.value,
        error = it?.errorCode?.let { error -> OffenceCodeError.valueOf(error.name) },
      )
    }

    offenceKey to OffenceCodeValues(
      ogrs3Weighting = buildOffenceCodeWeighting("ogrs3Weighting"),
      snsvStaticWeighting = buildOffenceCodeWeighting("snsvStaticWeighting"),
      snsvDynamicWeighting = buildOffenceCodeWeighting("snsvDynamicWeighting"),
      snsvVatpStaticWeighting = buildOffenceCodeWeighting("snsvVatpStaticWeighting"),
      snsvVatpDynamicWeighting = buildOffenceCodeWeighting("snsvVatpDynamicWeighting"),
      opdViolenceSexFlag = flags["opdViolSex"]?.value,
    )
  }
}
