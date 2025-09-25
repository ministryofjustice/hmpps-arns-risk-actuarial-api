package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.DYNAMIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.ScoreTypeResponse.STATIC
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType

enum class ScoreTypeResponse {

  STATIC,
  DYNAMIC,
  COMBINED,
}

fun ScoreType?.toScoreTypeResponse(): ScoreTypeResponse? = when (this) {
  ScoreType.STATIC -> STATIC
  ScoreType.DYNAMIC -> DYNAMIC
  null -> null
}
