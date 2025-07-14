package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.OffenceGroupParameters
import kotlin.NoSuchElementException

@Service
class OffenceGroupParametersService(val offenceGroupParameters: Map<String, OffenceGroupParameters>) {

  fun getOGRS3Weighting(key: String): Double = offenceGroupParameters[key]?.ogrs3Weighting
    ?: throw NoSuchElementException("No Match found on lookup: '$key'")
}
