package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun getDCSRPTotalContactAdultSexualSanctionsWeight(sanctions: Int) = when (sanctions) {
  1 -> 5
  2 -> 10
  in 3..Int.MAX_VALUE -> 15
  else -> 0
}

fun getDCSRPTotalContactChildSexualSanctionsWeight(sanctions: Int) = when (sanctions) {
  1 -> 3
  2 -> 6
  in 3..Int.MAX_VALUE -> 9
  else -> 0
}

fun getDCSRPTotalNonContactSexualOffencesWeight(totalNonContactSexualOffences: Int): Int = when (totalNonContactSexualOffences) {
  1 -> 2
  2 -> 4
  in 3..Int.MAX_VALUE -> 6
  else -> 0
}

fun getDCSRPAgeAtStartOfFollowupWeight(ageAtStartOfFollowup: Int): Int {
  if (ageAtStartOfFollowup >= 60) return 0
  return 14 - maxOf(0, (ageAtStartOfFollowup - 18) / 3)
}

fun getDCSRPAgeAtLastSanctionForSexualOffenceWeight(ageAtLastSanctionForSexualOffence: Int): Int = when (ageAtLastSanctionForSexualOffence) {
  in 16..17 -> 5
  in 18..Int.MAX_VALUE -> 10
  else -> 0
}

fun getDCSRPTotalNumberOfSanctionsForAllOffencesWeight(totalNumberOfSanctionsForAllOffences: Int): Int = when (totalNumberOfSanctionsForAllOffences) {
  in 2..Int.MAX_VALUE -> 6
  else -> 0
}

fun getDCSRPIsCurrentOffenceAgainstVictimStrangerWeight(isCurrentOffenceAgainstVictimStranger: Boolean?): Int = when (isCurrentOffenceAgainstVictimStranger) {
  true -> 4
  false, null -> 0
}

fun getDCSRPBand(dcsrp64PointScore: Int): RiskBand = when (dcsrp64PointScore) {
  in 0..21 -> RiskBand.LOW
  in 22..29 -> RiskBand.MEDIUM
  in 30..35 -> RiskBand.HIGH
  in 36..64 -> RiskBand.VERY_HIGH
  else -> throw IllegalArgumentException("Invalid OSP/DC 64 point score value: $dcsrp64PointScore")
}

fun getDCSRPScore(dcsrp64PointScore: Int): Double {
  if (dcsrp64PointScore == 0) {
    return 0.0
  }
  val z = -8.633 + (0.1598 * dcsrp64PointScore)
  return z.sigmoid()
}

fun getDCSRPRiskReduction(
  gender: Gender,
  supervisionStatus: SupervisionStatus,
  mostRecentOffenceDate: LocalDate?,
  dateOfMostRecentSexualOffence: LocalDate?,
  dateAtStartOfFollowup: LocalDate,
  assessmentDate: LocalDate,
  riskBand: RiskBand?,
): Boolean {
  if (gender == Gender.FEMALE) return false
  if (supervisionStatus == SupervisionStatus.CUSTODY) return false
  if (riskBand == null || riskBand == RiskBand.LOW || riskBand == RiskBand.NOT_APPLICABLE) return false

  if (mostRecentOffenceDate != null && assessmentDate.yearsSince(mostRecentOffenceDate) < 5) return false
  if (dateOfMostRecentSexualOffence != null && assessmentDate.yearsSince(dateOfMostRecentSexualOffence) < 5) return false
  if (assessmentDate.yearsSince(dateAtStartOfFollowup) < 5) return false

  return true
}

// Clean helper to remove inline date-math clutter
private fun LocalDate.yearsSince(otherDate: LocalDate): Int = ChronoUnit.YEARS.between(otherDate, this).toInt()

fun getDCSRPRiskBandReduction(ospRiskReduction: Boolean, riskBand: RiskBand): RiskBand {
  if (!ospRiskReduction) return riskBand
  return when (riskBand) {
    RiskBand.VERY_HIGH -> RiskBand.HIGH
    RiskBand.HIGH -> RiskBand.MEDIUM
    RiskBand.MEDIUM -> RiskBand.LOW
    else -> riskBand
  }
}
