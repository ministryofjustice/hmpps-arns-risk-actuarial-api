package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType

enum class StaticOrDynamic {
  STATIC,
  DYNAMIC,
}

fun StaticOrDynamic?.toScoreTypeResponse(): ScoreTypeResponse? = when (this) {
  StaticOrDynamic.STATIC -> ScoreTypeResponse.STATIC
  StaticOrDynamic.DYNAMIC -> ScoreTypeResponse.DYNAMIC
  else -> null
}
