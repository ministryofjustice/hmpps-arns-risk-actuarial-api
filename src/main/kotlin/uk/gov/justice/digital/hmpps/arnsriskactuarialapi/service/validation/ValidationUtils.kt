package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import kotlin.reflect.KProperty1

fun ArrayList<String>.addIfNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) == null) this.add(prop.name)
}

fun ArrayList<String>.addIfNotNull(request: RiskScoreRequest, prop: KProperty1<RiskScoreRequest, Any?>) {
  if (prop.get(request) != null) this.add(prop.name)
}

fun Map<KProperty1<RiskScoreRequest, Boolean?>, Any?>.getTrueKeys() = mapNotNull { (key, value) -> key.name.takeIf { value == true } }
