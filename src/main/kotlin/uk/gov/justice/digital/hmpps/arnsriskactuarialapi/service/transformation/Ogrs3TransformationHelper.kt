package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.AgeGroup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.OffenderConvictionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln

private const val MIN_CONVICTION_AGE = 10

fun getAgeAtCurrentConviction(
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate?, // can this actually be null ?
  ageAtFirstSanction: Int,
): Result<Int> = runCatching {
  if (dateOfCurrentConviction == null) {
    throw IllegalArgumentException("conviction date is null.")
  }

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

fun getOffenderAgeGroup(ageAtStartOfFollowup: Int) = getAgeGroup(ageAtStartOfFollowup)

fun getNumberOfPreviousSanctions(totalNumberOfSanctions: Int) = totalNumberOfSanctions.minus(1)

fun getOffenderConvictionStatus(totalNumberOfSanctions: Int) = if (totalNumberOfSanctions == 1) OffenderConvictionStatus.FIRST_TIME_OFFENDER else OffenderConvictionStatus.REPEAT_OFFENDER

fun getOffenderCopasScore(
  numberOfPreviousSanctions: Int,
  ageAtCurrentConviction: Int,
  ageAtFirstSanction: Int,
): Double {
  val numerator = 1.plus(numberOfPreviousSanctions).toDouble()
  val denominator = 10.plus(ageAtCurrentConviction).minus(ageAtFirstSanction).toDouble()
  return ln((((numerator / denominator))))
}

fun getAgeGenderParameter(ageGroup: AgeGroup, gender: Gender): Double {
  if (gender == Gender.MALE) {
    return when (ageGroup) {
      AgeGroup.TEN_TO_UNDER_TWELVE -> 0.0
      AgeGroup.TWELVE_TO_UNDER_FOURTEEN -> 0.08392
      AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN -> 0.07578
      AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN -> -0.0616
      AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE -> -0.6251
      AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE -> -1.0515
      AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY -> -1.1667
      AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE -> -1.326
      AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY -> -1.368
      AgeGroup.FORTY_TO_UNDER_FIFTY -> -1.4997
      AgeGroup.FIFTY_AND_OVER -> -2.0253
    }
  } else if (gender == Gender.FEMALE) {
    return when (ageGroup) {
      AgeGroup.TEN_TO_UNDER_TWELVE -> 0.785
      AgeGroup.TWELVE_TO_UNDER_FOURTEEN -> 0.61385
      AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN -> 0.66952
      AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN -> -0.9592
      AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE -> -0.8975
      AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE -> -1.0285
      AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY -> -1.0528
      AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE -> -1.1291
      AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY -> -1.4219
      AgeGroup.FORTY_TO_UNDER_FIFTY -> -1.5247
      AgeGroup.FIFTY_AND_OVER -> -2.4498
    }
  } else {
    throw IllegalArgumentException("Unhandled gender: $gender")
  }
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

private fun getAgeGroup(age: Int): AgeGroup = when {
  age < 0 -> throw IllegalArgumentException("Age cannot be negative")
  age < 10 -> throw IllegalArgumentException("Age must be age 10 or more")
  age in 10..11 -> AgeGroup.TEN_TO_UNDER_TWELVE
  age in 12..13 -> AgeGroup.TWELVE_TO_UNDER_FOURTEEN
  age in 14..15 -> AgeGroup.FOURTEEN_TO_UNDER_SIXTEEN
  age in 16..17 -> AgeGroup.SIXTEEN_TO_UNDER_EIGHTEEN
  age in 18..20 -> AgeGroup.EIGHTEEN_TO_UNDER_TWENTY_ONE
  age in 21..24 -> AgeGroup.TWENTY_ONE_TO_UNDER_TWENTY_FIVE
  age in 25..29 -> AgeGroup.TWENTY_FIVE_TO_UNDER_THIRTY
  age in 30..34 -> AgeGroup.THIRTY_TO_UNDER_THIRTY_FIVE
  age in 35..39 -> AgeGroup.THIRTY_FIVE_TO_UNDER_FORTY
  age in 40..49 -> AgeGroup.FORTY_TO_UNDER_FIFTY
  age >= 50 -> AgeGroup.FIFTY_AND_OVER
  else -> throw IllegalArgumentException("Unhandled age: $age")
}
