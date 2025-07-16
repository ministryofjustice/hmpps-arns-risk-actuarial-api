package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundTo5Decimals
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.ln

private const val MIN_CONVICTION_AGE = 10
private const val FIXED_CAPAS_VALUE = 1.25112
private const val FIXED_ONE_YEAR_SCORE_VALUE = 1.40256
private const val FIXED_TWO_YEAR_SCORE_VALUE = 2.1217

fun getAgeAtCurrentConviction(
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate,
  ageAtFirstSanction: Int,
): Int {
  if (dateOfCurrentConviction.isBefore(dateOfBirth)) {
    throw IllegalArgumentException("Conviction date cannot be before date of birth.")
  }

  val ageAtCurrentConviction = Period.between(dateOfBirth, dateOfCurrentConviction).years

  if (ageAtCurrentConviction < MIN_CONVICTION_AGE) {
    throw IllegalArgumentException("Age at current conviction must be at least $MIN_CONVICTION_AGE.")
  }

  if (ageAtFirstSanction > ageAtCurrentConviction) {
    throw IllegalArgumentException("Age at first sanction cannot be greater than age at current conviction.")
  }

  return ageAtCurrentConviction
}

fun getAgeAtStartOfFollowup(dateOfBirth: LocalDate, dateAtStartOfFollowup: LocalDate): Int = ChronoUnit.YEARS.between(dateOfBirth, dateAtStartOfFollowup).toInt()

fun getOffenderConvictionStatus(totalNumberOfSanctions: Int) = if (totalNumberOfSanctions == 1) OffenderConvictionStatus.FIRST_TIME_OFFENDER else OffenderConvictionStatus.REPEAT_OFFENDER

fun getOffenderCopasScore(
  numberOfPreviousSanctions: Int,
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
): Double {
  val numerator = 1.plus(numberOfPreviousSanctions).toDouble()
  val denominator = 10.plus(ageAtCurrentConviction).minus(ageAtFirstSanction).toDouble()

  if (denominator <= 0.0) {
    throw IllegalArgumentException("Invalid age values leading to non-positive denominator")
  }
  val logValue = ln(numerator / denominator)

  return logValue.roundTo5Decimals()
}

fun getConvictionStatusParameter(offenderConvictionStatus: OffenderConvictionStatus) = if (offenderConvictionStatus == OffenderConvictionStatus.REPEAT_OFFENDER) 0.46306 else 0.12614

fun getOffenderCopasFinalScore(offenderCopasScore: Double) = offenderCopasScore * FIXED_CAPAS_VALUE

fun getOgrs3OneYear(totalForAllParameters: Double) = getReoffendingProbability(totalForAllParameters, FIXED_ONE_YEAR_SCORE_VALUE).roundTo5Decimals()

fun getOgrs3TwoYear(totalForAllParameters: Double) = getReoffendingProbability(totalForAllParameters, FIXED_TWO_YEAR_SCORE_VALUE).roundTo5Decimals()

private fun getReoffendingProbability(totalForAllParameters: Double, fixedScore: Double): Double {
  val maxExpInput = 709.0 // above these values the calculation overflows
  val minExpInput = -745.0

  val exponent = fixedScore + totalForAllParameters
  return when {
    exponent >= maxExpInput || exponent <= minExpInput -> throw IllegalArgumentException("invalid inputs overflow exp operation")
    else -> {
      val expValue = exp(exponent)
      expValue / (1.0 + expValue)
    }
  }
}

fun getRiskBand(percentage: Int): RiskBand = when (percentage) {
  in 0..49 -> RiskBand.LOW
  in 50..74 -> RiskBand.MEDIUM
  in 75..89 -> RiskBand.HIGH
  in 90..Int.MAX_VALUE -> RiskBand.VERY_HIGH
  else -> throw IllegalArgumentException("Unhandled ogrs3TwoYear percent: $percentage")
}

fun getAgeGenderParameter(age: Int, gender: Gender): Double {
  val maleParams = listOf(
    10..11 to 0.0,
    12..13 to 0.08392,
    14..15 to 0.07578,
    16..17 to -0.0616,
    18..20 to -0.6251,
    21..24 to -1.0515,
    25..29 to -1.1667,
    30..34 to -1.326,
    35..39 to -1.368,
    40..49 to -1.4997,
    50..Int.MAX_VALUE to -2.0253,
  )

  val femaleParams = listOf(
    10..11 to 0.785,
    12..13 to 0.61385,
    14..15 to 0.66952,
    16..17 to -0.9592,
    18..20 to -0.8975,
    21..24 to -1.0285,
    25..29 to -1.0528,
    30..34 to -1.1291,
    35..39 to -1.4219,
    40..49 to -1.5247,
    50..Int.MAX_VALUE to -2.4498,
  )

  val params = when (gender) {
    Gender.MALE -> maleParams
    Gender.FEMALE -> femaleParams
  }

  return params.firstOrNull { age in it.first }
    ?.second
    ?: throw IllegalArgumentException("Unhandled age: $age")
}
