package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConvictions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.ln

fun getAgeAt(stage: String, dateOfBirth: LocalDate, dateAtStage: LocalDate, lowest: Int): Int = Period.between(dateOfBirth, dateAtStage).years
  .apply {
    require(!dateAtStage.isBefore(dateOfBirth)) { "${stage.replaceFirstChar(Char::titlecase)} cannot be before date of birth." }
    require(this >= lowest) { "Age at $stage cannot be less than $lowest" }
  }

fun get2YearInterceptWeight(isSNSVDynamic: Boolean): BigDecimal = when (isSNSVDynamic) {
  false -> SeriousViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient
  true -> SeriousViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient
}

fun getAgeGenderPolynomialWeight(
  gender: Gender,
  dateOfBirth: LocalDate,
  dateAtStartOfFollowup: LocalDate,
  isSNSVDynamic: Boolean,
): BigDecimal = getAgeAt("date at start of followup", dateOfBirth, dateAtStartOfFollowup, 10).toBigDecimal()
  .let { age ->
    val staticMaleCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorStatic.AAI_MALE.coefficient,
      SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
    )

    val dynamicMaleCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
      SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
    )

    val staticFemaleCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorStatic.AAI_FEMALE.coefficient,
      SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
    )

    val dynamicFemaleCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
      SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
    )

    when (gender) {
      Gender.MALE -> (if (isSNSVDynamic) dynamicMaleCoefficients else staticMaleCoefficients).let { coefficients ->
        age * calculatePolynomial(coefficients, age)
      }

      Gender.FEMALE -> (if (isSNSVDynamic) dynamicFemaleCoefficients else staticFemaleCoefficients).let { coefficients ->
        age * calculatePolynomial(coefficients, age)
      }
    }
  }

fun getGenderWeight(
  gender: Gender,
  isSNSVDynamic: Boolean,
): BigDecimal = when (gender) {
  Gender.MALE -> BigDecimal.ZERO
  Gender.FEMALE -> if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.FEMALE.coefficient else SeriousViolentReoffendingPredictorStatic.FEMALE.coefficient
}

fun getOneSanctionsWeight(totalNumberOfSanctionsForAllOffences: Int, isSNSVDynamic: Boolean): BigDecimal {
  val staticCoefficients = SeriousViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient
  val dynamicCoefficients = SeriousViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient
  if (totalNumberOfSanctionsForAllOffences != 1) return BigDecimal.ZERO
  return if (isSNSVDynamic) dynamicCoefficients else staticCoefficients
}

fun getTwoSanctionsWeight(totalNumberOfSanctionsForAllOffences: Int, isSNSVDynamic: Boolean): BigDecimal {
  val staticCoefficients = SeriousViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient
  val dynamicCoefficients = SeriousViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient
  if (totalNumberOfSanctionsForAllOffences != 2) return BigDecimal.ZERO
  return if (isSNSVDynamic) dynamicCoefficients else staticCoefficients
}

fun getTotalSanctionWeight(totalNumberOfSanctionsForAllOffences: Int, isSNSVDynamic: Boolean): BigDecimal = when (isSNSVDynamic) {
  true -> SeriousViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient
  false -> SeriousViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient
}.let { weights ->
  when {
    totalNumberOfSanctionsForAllOffences > 0 -> totalNumberOfSanctionsForAllOffences.toBigDecimal() * weights
    else -> throw IllegalArgumentException("Invalid total number of sanctions value: $totalNumberOfSanctionsForAllOffences")
  }
}

fun getYearsBetweenFirstAndSecondSanctionWeight(
  totalNumberOfSanctionsForAllOffences: Int,
  gender: Gender,
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate,
  ageAtFirstSanction: Int,
  isSNSVDynamic: Boolean,
): BigDecimal {
  val staticYearBetweenFirstAndSecondSanctionCoefficients = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient,
  )

  val dynamicYearBetweenFirstAndSecondSanctionCoefficients = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient,
  )

  if (totalNumberOfSanctionsForAllOffences != 2) return BigDecimal.ZERO

  val ageAtCurrentConviction = getAgeAt("current conviction date", dateOfBirth, dateOfCurrentConviction, 10)
  val yearsBetweenSanctions = ageAtCurrentConviction - ageAtFirstSanction
  require(yearsBetweenSanctions >= 0) { "Years between first and second sanction cannot be a negative" }

  return yearsBetweenSanctions.toBigDecimal() * when (isSNSVDynamic) {
    true -> dynamicYearBetweenFirstAndSecondSanctionCoefficients
    false -> staticYearBetweenFirstAndSecondSanctionCoefficients
  }[gender]!!
}

fun getMonthsSinceLastSanctionWeight(
  dateAtStartOfFollowup: LocalDate,
  assessmentDate: LocalDate,
  isSNSVDynamic: Boolean,
): BigDecimal {
  val staticMonthsSinceLastSanctionCoefficients = arrayOf(
    SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
    SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
    SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
    SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
  )

  val dynamicMonthsSinceLastSanctionCoefficients = arrayOf(
    SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
    SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
    SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
    SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
  )

  if (dateAtStartOfFollowup >= assessmentDate) return BigDecimal.ZERO

  val monthsSinceLastSanctionPreCheck = ChronoUnit.MONTHS.between(dateAtStartOfFollowup, assessmentDate).toInt()
  val monthsSinceLastSanction = if (monthsSinceLastSanctionPreCheck >= 36) 36 else monthsSinceLastSanctionPreCheck
  val coeffs =
    if (isSNSVDynamic) dynamicMonthsSinceLastSanctionCoefficients else staticMonthsSinceLastSanctionCoefficients
  return monthsSinceLastSanction.toBigDecimal() * calculatePolynomial(coeffs, monthsSinceLastSanction.toBigDecimal())
}

fun getThreePlusSanctionsWeight(
  gender: Gender,
  totalNumberOfSanctionsForAllOffences: Int,
  ageAtFirstSanction: Int,
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate,
  isSNSVDynamic: Boolean,
): BigDecimal {
  val staticThreePlusSanctionsWeight = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient,
  )

  val dynamicThreePlusSanctionsWeight = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient,
  )

  if (totalNumberOfSanctionsForAllOffences < 3) return BigDecimal.ZERO
  val ageAtCurrentConviction = getAgeAt("current conviction date", dateOfBirth, dateOfCurrentConviction, 10)
  val x1 = ageAtCurrentConviction - ageAtFirstSanction + 12
  val x2 = totalNumberOfSanctionsForAllOffences / x1.toDouble()
  val x3 = ln(x2)
  return x3.toBigDecimal() * (if (isSNSVDynamic) dynamicThreePlusSanctionsWeight else staticThreePlusSanctionsWeight)[gender]!!
}

fun getNeverViolentHistoryWeight(
  totalNumberOfViolentSanctions: Int,
  gender: Gender,
  isSNSVDynamic: Boolean,
): BigDecimal {
  val staticCoefficients = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_FEMALE.coefficient,
  )

  val dynamicCoefficients = mapOf(
    Gender.MALE to SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_MALE.coefficient,
    Gender.FEMALE to SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_FEMALE.coefficient,
  )

  if (totalNumberOfViolentSanctions != 0) return BigDecimal.ZERO

  return if (isSNSVDynamic) dynamicCoefficients[gender]!! else staticCoefficients[gender]!!
}

fun getOnceViolentHistoryWeight(
  totalNumberOfViolentSanctions: Int,
  isSNSVDynamic: Boolean,
): BigDecimal {
  val staticCoefficients = SeriousViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient
  val dynamicCoefficients = SeriousViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient
  if (totalNumberOfViolentSanctions != 1) return BigDecimal.ZERO
  return if (isSNSVDynamic) dynamicCoefficients else staticCoefficients
}

fun getViolentSanctionsWeight(
  totalNumberOfViolentSanctions: Int,
  isSNSVDynamic: Boolean,
): BigDecimal = (
  if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient else SeriousViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient
  )
  .let { nonGenderedWeights ->
    when {
      totalNumberOfViolentSanctions == 0 -> BigDecimal.ZERO
      totalNumberOfViolentSanctions > 0 -> nonGenderedWeights * totalNumberOfViolentSanctions.toBigDecimal()
      else -> throw IllegalArgumentException("Invalid total number of violent sanctions value: $totalNumberOfViolentSanctions")
    }
  }

fun getViolenceRateWeight(
  dateOfBirth: LocalDate,
  dateOfCurrentConviction: LocalDate,
  ageAtFirstSanction: Int,
  totalNumberOfViolentSanctions: Int,
  isSNSVDynamic: Boolean,
): BigDecimal {
  if (totalNumberOfViolentSanctions == 0) return BigDecimal.ZERO
  val ageAtCurrentConviction = getAgeAt("current conviction", dateOfBirth, dateOfCurrentConviction, 10)
  val x1 = ageAtCurrentConviction - ageAtFirstSanction + 30
  val x2 = totalNumberOfViolentSanctions / x1.toDouble()
  val x3 = ln(x2)
  val c1 =
    if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.VIOLENT_RATE.coefficient else SeriousViolentReoffendingPredictorStatic.VIOLENT_RATE.coefficient
  return x3.toBigDecimal() * c1
}

// SNSV Dynamic Additions
fun didOffenceInvolveCarryingOrUsingWeaponWeight(carryingOrUsingWeapon: Boolean): BigDecimal = if (carryingOrUsingWeapon) SeriousViolentReoffendingPredictorDynamic.CARRY_OR_USE_WEAPON.coefficient else BigDecimal.ZERO

fun suitabilityOfAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): BigDecimal = SeriousViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient * suitabilityOfAccommodation.score.toBigDecimal()

fun isUnemployedWeight(isUnemployed: Boolean): BigDecimal = if (isUnemployed) SeriousViolentReoffendingPredictorDynamic.UNEMPLOYED.coefficient else BigDecimal.ZERO

fun chronicDrinkingProblemsWeight(currentAlcoholUseProblems: ProblemLevel): BigDecimal = currentAlcoholUseProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

fun temperControlWeight(temperProblems: ProblemLevel): BigDecimal = temperProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.TEMPER.coefficient

fun proCriminalAttitudesWeight(proCriminalAttitudes: ProblemLevel): BigDecimal = proCriminalAttitudes.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient

fun previousConvictionsWeight(previousConvictions: List<PreviousConvictions>): BigDecimal = previousConvictions.sumOf { conviction -> conviction.snsvDynamicWeight }
