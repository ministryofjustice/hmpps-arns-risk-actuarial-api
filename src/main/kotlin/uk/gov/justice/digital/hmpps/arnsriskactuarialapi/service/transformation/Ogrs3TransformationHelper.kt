package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.scale.toCommonScale
import java.math.BigDecimal
import java.math.MathContext
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln

private const val MIN_CONVICTION_AGE = 10

fun getAgeAtCurrentConviction(
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate,
  ageAtFirstSanction: Int,
): Result<Int> = runCatching {
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

  ageAtCurrentConviction
}

fun getAgeAtStartOfFollowup(dateOfBirth: LocalDate, dateAtStartOfFollowup: LocalDate): Int = ChronoUnit.YEARS.between(dateOfBirth, dateAtStartOfFollowup).toInt()

fun getNumberOfPreviousSanctions(totalNumberOfSanctions: Int) = totalNumberOfSanctions.minus(1)

fun getOffenderConvictionStatus(totalNumberOfSanctions: Int) = if (totalNumberOfSanctions == 1) OffenderConvictionStatus.FIRST_TIME_OFFENDER else OffenderConvictionStatus.REPEAT_OFFENDER

fun getOffenderCopasScore(
  numberOfPreviousSanctions: Int,
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
): BigDecimal {
  val numerator = 1.plus(numberOfPreviousSanctions).toDouble()
  val denominator = 10.plus(ageAtCurrentConviction).minus(ageAtFirstSanction).toDouble()

  if (denominator <= 0.0) {
    throw IllegalArgumentException("Invalid age values leading to non-positive denominator")
  }
  val logValue = ln(numerator / denominator)

  return BigDecimal(logValue, MathContext.DECIMAL64).toCommonScale()
}

fun getConvictionStatusParameter(offenderConvictionStatus: OffenderConvictionStatus) = if (offenderConvictionStatus == OffenderConvictionStatus.REPEAT_OFFENDER) 0.46306 else 0.12614

fun getOffenderCopasScore(offenderCopasScore: Double) = offenderCopasScore * 1.25112

fun getOgrs3OneYear(totalForAllParameters: Double) = getReoffendingProbability(totalForAllParameters, 1.40256)
fun getOgrs3TwoYear(totalForAllParameters: Double) = getReoffendingProbability(totalForAllParameters, 2.1217)

fun getRiskBand(ogrs3TwoYear: Double): RiskBand {
  val percentage = floor(ogrs3TwoYear * 100).toInt()
  return when (percentage) {
    in 0..49 -> RiskBand.LOW
    in 50..74 -> RiskBand.MEDIUM
    in 75..89 -> RiskBand.HIGH
    in 90..Int.MAX_VALUE -> RiskBand.VERY_HIGH
    else -> throw IllegalArgumentException("Unhandled ogrs3TwoYear percent: $percentage")
  }
}

private fun getReoffendingProbability(totalForAllParameters: Double, x: Double): Double {
  val numerator = exp(x.plus(totalForAllParameters))
  val denominator = 1.plus(numerator)
  return numerator / denominator
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
