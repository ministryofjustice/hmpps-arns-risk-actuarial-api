package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.CombinedSeriousReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import java.math.BigDecimal

object CombinedSeriousReoffendingPredictorTransformationHelper {
  fun getFemaleWeight(request: RiskScoreRequest): BigDecimal? = if (request.gender == Gender.FEMALE && request.hasEverCommittedSexualOffence!!) {
    CombinedSeriousReoffendingPredictor.FEMALE_SEXUAL_OFFENDER.coefficient
  } else {
    null
  }

  fun getScore(
    seriousViolentPercentageScore: Double?,
    directContactSexualReoffendingPredictorPercentageScore: Double?,
    iicsrpPercentageScore: Double?,
    femaleSexualOffenderCoefficient: Double?,
  ): Double? {
    if (
      seriousViolentPercentageScore == null &&
      directContactSexualReoffendingPredictorPercentageScore == null &&
      iicsrpPercentageScore == null &&
      femaleSexualOffenderCoefficient == null
    ) {
      return null
    }

    return listOf(
      seriousViolentPercentageScore!!,
      directContactSexualReoffendingPredictorPercentageScore!!,
      iicsrpPercentageScore!!,
      femaleSexualOffenderCoefficient?.asDoublePercentage() ?: 0.0,
    ).sum().roundToNDecimals(2).sanitisePercentage()
  }

  fun getBand(score: Double?): RiskBand? = when {
    score == null -> null
    score < 0.0 -> RiskBand.NOT_APPLICABLE
    score < 1.0 -> RiskBand.LOW
    score < 3.0 -> RiskBand.MEDIUM
    score < 6.9 -> RiskBand.HIGH
    else -> RiskBand.VERY_HIGH
  }
}
