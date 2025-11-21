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

    fun toOffenceCodeWeighting(name: String) = weightings[name].let {
      OffenceCodeWeighting(
        value = it?.value,
        error = it?.errorCode?.let { code -> OffenceCodeError.valueOf(code.name) },
      )
    }

    offenceKey to OffenceCodeValues(
      ogrs3Weighting = toOffenceCodeWeighting("ogrs3Weighting"),
      snsvStaticWeighting = toOffenceCodeWeighting("snsvStaticWeighting"),
      snsvDynamicWeighting = toOffenceCodeWeighting("snsvDynamicWeighting"),
      snsvVatpStaticWeighting = toOffenceCodeWeighting("snsvVatpStaticWeighting"),
      snsvVatpDynamicWeighting = toOffenceCodeWeighting("snsvVatpDynamicWeighting"),
      opdViolenceSexFlag = flags["opdViolSex"]?.value,
    )
  }
}
