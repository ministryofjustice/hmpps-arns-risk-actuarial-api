package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConvictions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ln

fun get2YearInterceptWeight(staticOrDynamic: StaticOrDynamic): BigDecimal = when (staticOrDynamic) {
  StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient
  StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient
}

fun getAgeGenderPolynomialWeight(
  gender: Gender,
  ageAtStartOfFollowup: Int,
  staticOrDynamic: StaticOrDynamic,
): BigDecimal {
  val coefficients: Array<BigDecimal> = when (gender) {
    Gender.MALE -> when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> arrayOf(
        SeriousViolentReoffendingPredictorStatic.AAI_MALE.coefficient,
        SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
      )

      StaticOrDynamic.DYNAMIC -> arrayOf(
        SeriousViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
        SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
      )
    }

    Gender.FEMALE -> when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> arrayOf(
        SeriousViolentReoffendingPredictorStatic.AAI_FEMALE.coefficient,
        SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
      )

      StaticOrDynamic.DYNAMIC -> arrayOf(
        SeriousViolentReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
        SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
      )
    }
  }

  return calculatePolynomial(coefficients, ageAtStartOfFollowup.toBigDecimal())
}

fun getFemaleWeight(staticOrDynamic: StaticOrDynamic, gender: Gender): BigDecimal = when (gender) {
  Gender.MALE -> BigDecimal.ZERO
  Gender.FEMALE -> when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.FEMALE.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.FEMALE.coefficient
  }
}

fun getFirstSanctionWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfSanctionsForAllOffences: Int,
): BigDecimal = if (totalNumberOfSanctionsForAllOffences == 1) {
  when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient
  }
} else {
  BigDecimal.ZERO
}

fun getSecondSanctionWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfSanctionsForAllOffences: Int,
): BigDecimal = if (totalNumberOfSanctionsForAllOffences == 2) {
  when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient
  }
} else {
  BigDecimal.ZERO
}

fun getTotalSanctionWeight(staticOrDynamic: StaticOrDynamic, totalNumberOfSanctionsForAllOffences: Int): BigDecimal {
  val coefficient: BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient
  }

  return totalNumberOfSanctionsForAllOffences.toBigDecimal() * coefficient
}

fun getGapBetweenFirstAndSecondSanctionWeight(
  staticOrDynamic: StaticOrDynamic,
  gender: Gender,
  ageAtFirstSanction: Int,
  ageAtCurrentSanction: Int,
  totalNumberOfSanctionsForAllOffences: Int,
): BigDecimal {
  if (totalNumberOfSanctionsForAllOffences != 2) return BigDecimal.ZERO

  val firstToSecondSanctionYears: Int = ageAtCurrentSanction - ageAtFirstSanction

  val coefficient: BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
    }

    StaticOrDynamic.DYNAMIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
    }
  }

  return firstToSecondSanctionYears.toBigDecimal() * coefficient
}

fun getOffenceFreeMonthsPolynomial(
  staticOrDynamic: StaticOrDynamic,
  assessmentDate: LocalDate,
  dateAtStartOfFollowupCalculated: LocalDate,
): BigDecimal {
  if (assessmentDate.isBefore(dateAtStartOfFollowupCalculated)) return BigDecimal.ZERO

  val monthsBetweenAssessmentAndFollowup = ChronoUnit.MONTHS.between(
    dateAtStartOfFollowupCalculated,
    assessmentDate,
  ).coerceAtMost(36)

  val coefficients: Array<BigDecimal> = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> arrayOf(
      SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
      SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
      SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
      SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
    )

    StaticOrDynamic.DYNAMIC -> arrayOf(
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
    )
  }

  return calculatePolynomial(coefficients, monthsBetweenAssessmentAndFollowup.toBigDecimal())
}

fun getThreePlusSanctionsWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfSanctionsForAllOffences: Int,
  gender: Gender,
  ageAtFirstSanction: Int,
  ageAtCurrentSanction: Int,
): BigDecimal {
  if (totalNumberOfSanctionsForAllOffences < 3) return BigDecimal.ZERO

  val lengthOfCareer = ageAtCurrentSanction - ageAtFirstSanction + 12

  val coefficient = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient
    }

    StaticOrDynamic.DYNAMIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient
    }
  }

  val totalSanctionsRatio = totalNumberOfSanctionsForAllOffences.toDouble() / lengthOfCareer
  val naturalLog = ln(totalSanctionsRatio)

  return naturalLog.toBigDecimal() * coefficient
}

fun getNeverViolentHistoryWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfViolentSanctions: Int,
  gender: Gender,
): BigDecimal {
  if (totalNumberOfViolentSanctions != 0) return BigDecimal.ZERO

  return when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_FEMALE.coefficient
    }

    StaticOrDynamic.DYNAMIC -> when (gender) {
      Gender.MALE -> SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_MALE.coefficient
      Gender.FEMALE -> SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_FEMALE.coefficient
    }
  }
}

fun getOnceViolentHistoryWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfViolentSanctions: Int,
): BigDecimal = if (totalNumberOfViolentSanctions == 1) {
  when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient
  }
} else {
  BigDecimal.ZERO
}

fun getViolentSanctionsWeight(
  staticOrDynamic: StaticOrDynamic,
  totalNumberOfViolentSanctions: Int,
): BigDecimal = if (totalNumberOfViolentSanctions != 0) {
  val coefficient = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient
  }
  coefficient * totalNumberOfViolentSanctions.toBigDecimal()
} else {
  BigDecimal.ZERO
}

fun getViolenceRateWeight(
  staticOrDynamic: StaticOrDynamic,
  ageAtFirstSanction: Int,
  totalNumberOfViolentSanctions: Int,
  ageAtCurrentSanction: Int,
): BigDecimal {
  if (totalNumberOfViolentSanctions == 0) return BigDecimal.ZERO

  val coefficient = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.VIOLENT_RATE.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.VIOLENT_RATE.coefficient
  }

  val lengthOfCareer = ageAtCurrentSanction - ageAtFirstSanction + 30
  val violentSanctionsRatio = totalNumberOfViolentSanctions / lengthOfCareer.toDouble()
  val naturalLog = ln(violentSanctionsRatio)

  return naturalLog.toBigDecimal() * coefficient
}

// SNSV Dynamic Additions
fun didOffenceInvolveCarryingOrUsingWeaponWeight(carryingOrUsingWeapon: Boolean): BigDecimal = if (carryingOrUsingWeapon) SeriousViolentReoffendingPredictorDynamic.CARRY_OR_USE_WEAPON.coefficient else BigDecimal.ZERO

fun suitabilityOfAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): BigDecimal = SeriousViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient * suitabilityOfAccommodation.score.toBigDecimal()

fun isUnemployedWeight(isUnemployed: Boolean): BigDecimal = if (isUnemployed) SeriousViolentReoffendingPredictorDynamic.UNEMPLOYED.coefficient else BigDecimal.ZERO

fun chronicDrinkingProblemsWeight(currentAlcoholUseProblems: ProblemLevel): BigDecimal = currentAlcoholUseProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

fun temperControlWeight(temperProblems: ProblemLevel): BigDecimal = temperProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.TEMPER.coefficient

fun proCriminalAttitudesWeight(proCriminalAttitudes: ProblemLevel): BigDecimal = proCriminalAttitudes.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient

fun previousConvictionsWeight(previousConvictions: List<PreviousConvictions>): BigDecimal = previousConvictions.sumOf { conviction -> conviction.snsvDynamicWeight }
