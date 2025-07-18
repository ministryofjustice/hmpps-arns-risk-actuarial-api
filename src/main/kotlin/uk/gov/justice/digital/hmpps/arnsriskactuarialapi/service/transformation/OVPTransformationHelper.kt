package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp.OVPRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asPercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import kotlin.math.ceil
import kotlin.math.exp

private const val FIXED_ONE_YEAR_COEFFICIENT = 4.5215
private const val FIXED_TWO_YEAR_COEFFICIENT = 3.8773

// Boolean to Int Score transformers
private fun Boolean.booleanToScore(): Int = when (this) {
  false -> 0
  true -> 2
}

private fun Boolean.booleanToInverseScore(): Int = when (this) {
  false -> 4
  true -> 0
}

// Int to Int Score transformers
private fun Int.numberToScore(): Int = if (this == 2) 4 else 0

fun getAlcoholMisuseWeighted(request: OVPRequestValidated): Int = listOf(
  request.alcoholIsCurrentUseAProblem.ordinal,
  request.alcoholExcessive6Months.ordinal,
).sum().let { ceil(it * 2.5).toInt() }

fun getCurrentAccommodationOffendersScore(request: OVPRequestValidated): Int = request.currentAccommodation.booleanToScore()

fun getEmploymentStatusOffendersScore(request: OVPRequestValidated): Int = request.employmentStatus.booleanToScore()

fun getImpactOfOffendingOnOthersWeighted(request: OVPRequestValidated): Int = request.impactOfOffendingOnOthers.booleanToInverseScore()

fun getCurrentPsychiatricTreatmentOrPendingWeighted(request: OVPRequestValidated): Int = when (request.currentPsychiatricTreatmentOrPending) {
  true -> 4
  false -> 0
}

fun getCurrentAccommodationWeightedOVP(currentAccommodationOffendersScore: Int): Int = currentAccommodationOffendersScore.numberToScore()

fun getEmploymentStatusWeightedOVP(employmentStatusScore: Int): Int = employmentStatusScore.numberToScore()

fun getAgeAtStartOfFollowup(request: OVPRequestValidated): Int = getAgeAtStartOfFollowup(request.dateOfBirth, request.dateAtStartOfFollowup)

fun getAnyPreviousSanctionsWeighted(request: OVPRequestValidated): Int = when (request.totalNumberOfSanctions) {
  0 -> 0
  else -> 5
}

fun getGenderWeighted(request: OVPRequestValidated): Int = when (request.gender) {
  Gender.MALE -> 5
  else -> 0
}

fun getTotalNumberOfViolentSanctionsWeighted(sanctions: Int) = when (sanctions) {
  0 -> 0
  1 -> 4
  2 -> 7
  3 -> 9
  4 -> 11
  5 -> 12
  6 -> 13
  7 -> 14
  8 -> 15
  9 -> 16
  10 -> 17
  11 -> 18
  12 -> 19
  13 -> 20
  14 -> 21
  15 -> 22
  16 -> 23
  17 -> 24
  in 18..Int.MAX_VALUE -> 25
  else -> throw IllegalArgumentException("Invalid violent sanctions value: $sanctions")
}

fun getTotalNumberOfNonViolentSanctionsWeighted(sanctions: Int) = when (sanctions) {
  in 0..2 -> 0
  in 3..4 -> 2
  in 5..10 -> 3
  in 11..20 -> 4
  in 21..Int.MAX_VALUE -> 5
  else -> throw IllegalArgumentException("Invalid non violent sanctions value: $sanctions")
}

fun getOffenderAgeGroupOVP(ageAtStartOfFollowup: Int) = when (ageAtStartOfFollowup) {
  in 18..19 -> 20
  in 20..21 -> 17
  in 22..23 -> 14
  in 24..25 -> 12
  in 26..30 -> 10
  in 31..35 -> 8
  in 36..40 -> 6
  in 41..45 -> 4
  in 46..50 -> 2
  in 51..Int.MAX_VALUE -> 0
  else -> throw IllegalArgumentException("Invalid ageAtStartOfFollowup value: $ageAtStartOfFollowup")
}

fun calculateOVPPercentageOneYear(totalOVPScore: Int) = calculateOVPRate(totalOVPScore, FIXED_ONE_YEAR_COEFFICIENT).asPercentage().sanitisePercentage()
fun calculateOVPPercentageTwoYears(totalOVPScore: Int) = calculateOVPRate(totalOVPScore, FIXED_TWO_YEAR_COEFFICIENT).asPercentage().sanitisePercentage()

fun calculateOVPRate(totalOVPScore: Int, yearlyCoefficient: Double): Double {
  val exponent = 0.0722 * totalOVPScore - yearlyCoefficient
  val expValue = exp(exponent)
  return (expValue / (1 + expValue))
}

fun getOVPBand(percentage: Int): RiskBand = when (percentage) {
  in 1..29 -> RiskBand.LOW
  in 30..59 -> RiskBand.MEDIUM
  in 60..79 -> RiskBand.HIGH
  in 80..99 -> RiskBand.VERY_HIGH
  else -> throw IllegalArgumentException("Unhandled OVP percentage value: $percentage")
}
