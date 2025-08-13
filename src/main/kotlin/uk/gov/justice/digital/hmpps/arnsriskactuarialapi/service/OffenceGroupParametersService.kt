package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.OffenceGroupParameters

@Service
class OffenceGroupParametersService(val offenceGroupParameters: Map<String, OffenceGroupParameters>) {

  fun getOGRS3Weighting(offenceKey: String): Double = offenceGroupParameters[offenceKey]?.ogrs3Weighting
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")

  fun isViolentOrSexualType(offenceKey: String): Boolean = offenceGroupParameters[offenceKey]?.opdViolSex
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")

  fun getSNSVStaticWeighting(offenceKey: String): Double = offenceGroupParameters[offenceKey]?.snsvStaticWeighting
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")

  fun getSNSVDynamicWeighting(offenceKey: String): Double = offenceGroupParameters[offenceKey]?.snsvDynamicWeighting
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")

  fun getSNSVVATPStaticWeighting(offenceKey: String): Double = offenceGroupParameters[offenceKey]?.snsvVATPStaticWeighting
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")

  fun getSNSVVATPDynamicWeighting(offenceKey: String): Double = offenceGroupParameters[offenceKey]?.snsvVATPDynamicWeighting
    ?: throw NoSuchElementException("No Match found on lookup: '$offenceKey'")
}
