package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.config.OffenceGroupParameters

@Service
class OffenceGroupParametersService(val offenceGroupParameters: Map<String, OffenceGroupParameters>) {

  fun getOGRS3Weighting(offenceKey: String): Double? = offenceGroupParameters[offenceKey]?.ogrs3Weighting

  fun isViolentOrSexualType(offenceKey: String): Boolean? = offenceGroupParameters[offenceKey]?.opdViolSex

  fun getSNSVStaticWeighting(offenceKey: String): Double? = offenceGroupParameters[offenceKey]?.snsvStaticWeighting

  fun getSNSVDynamicWeighting(offenceKey: String): Double? = offenceGroupParameters[offenceKey]?.snsvDynamicWeighting

  fun getSNSVVATPStaticWeighting(offenceKey: String): Double? = offenceGroupParameters[offenceKey]?.snsvVATPStaticWeighting

  fun getSNSVVATPDynamicWeighting(offenceKey: String): Double? = offenceGroupParameters[offenceKey]?.snsvVATPDynamicWeighting
}
