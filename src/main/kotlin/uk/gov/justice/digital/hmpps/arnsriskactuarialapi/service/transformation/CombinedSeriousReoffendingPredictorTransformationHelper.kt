package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.CombinedSeriousReoffendingPredictor
import java.math.BigDecimal

fun getCSRPScore(
  gender: Gender,
  sexualOffendingHistory: Boolean,
  seriousViolentPercentageScore: BigDecimal,
  directContactSexualReoffendingPredictorPercentageScore: BigDecimal,
  iicsrpPercentageScore: BigDecimal,
): BigDecimal {
  val femaleSexualOffenderCoefficient = if (gender == Gender.FEMALE && sexualOffendingHistory) {
    CombinedSeriousReoffendingPredictor.FEMALE_SEXUAL_OFFENDER.coefficient
  } else {
    BigDecimal.ZERO
  }

  return seriousViolentPercentageScore
    .add(directContactSexualReoffendingPredictorPercentageScore)
    .add(iicsrpPercentageScore)
    .add(femaleSexualOffenderCoefficient)
    .min(BigDecimal("99.99"))
}

fun getCSRPBand(rsrScore: BigDecimal): RiskBand = when {
  rsrScore < BigDecimal.ZERO -> RiskBand.NOT_APPLICABLE
  rsrScore < BigDecimal.ONE -> RiskBand.LOW
  rsrScore < BigDecimal("3") -> RiskBand.MEDIUM
  rsrScore < BigDecimal("6.9") -> RiskBand.HIGH
  else -> RiskBand.VERY_HIGH
}
