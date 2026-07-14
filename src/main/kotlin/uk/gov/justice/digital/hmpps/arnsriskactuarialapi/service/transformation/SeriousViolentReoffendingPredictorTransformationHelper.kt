package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeCacheService
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.getSeriousViolentReoffendingPredictorDynamicOffenceCodeCoefficient
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.getSeriousViolentReoffendingPredictorStaticOffenceCodeCoefficient
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.SeriousViolentReoffendingPredictorConstant
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ln

object SeriousViolentReoffendingPredictorTransformationHelper {

  fun get2YearInterceptWeight(staticOrDynamic: StaticOrDynamic): BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient
    StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient
  }

  fun getAgeGenderPolynomialWeight(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    ageAtStartOfFollowup: Int,
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

  fun getGenderWeight(staticOrDynamic: StaticOrDynamic, gender: Gender): BigDecimal = when (gender) {
    Gender.MALE -> BigDecimal.ZERO
    Gender.FEMALE -> when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.FEMALE.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.FEMALE.coefficient
    }
  }

  fun getOffenceGroupWeight(offenceCodeCacheService: OffenceCodeCacheService, staticOrDynamic: StaticOrDynamic, currentOffenceCode: String): BigDecimal {
    // The currentOffenceCode has been prevalidated but just in case throw an error if this returns null
    val actuarialCategory = offenceCodeCacheService.getActuarialCategory(currentOffenceCode)
      ?: throw IllegalArgumentException("Offence code mapping for $currentOffenceCode not found, ensure this is validated before the calculation")
    return when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> getSeriousViolentReoffendingPredictorStaticOffenceCodeCoefficient(actuarialCategory, currentOffenceCode)
      StaticOrDynamic.DYNAMIC -> getSeriousViolentReoffendingPredictorDynamicOffenceCodeCoefficient(actuarialCategory, currentOffenceCode)
    }
  }

  fun getFirstSanctionWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
  ): BigDecimal {
    if (totalNumberOfSanctionsForAllOffences != 1) {
      return BigDecimal.ZERO
    }
    return when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient
    }
  }

  fun getSecondSanctionWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
  ): BigDecimal {
    if (totalNumberOfSanctionsForAllOffences != 2) {
      return BigDecimal.ZERO
    }
    return when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient
    }
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
    if (totalNumberOfSanctionsForAllOffences != 2) {
      return BigDecimal.ZERO
    }

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

  fun getOffenceFreeMonthsPolynomialWeight(
    staticOrDynamic: StaticOrDynamic,
    assessmentDate: LocalDate,
    dateAtStartOfFollowup: LocalDate,
  ): BigDecimal {
    // They are not in the community
    if (assessmentDate.isBefore(dateAtStartOfFollowup)) {
      return BigDecimal.ZERO
    }

    val monthsBetweenAssessmentAndFollowup = ChronoUnit.MONTHS.between(
      dateAtStartOfFollowup,
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

  fun getCopasWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
  ): BigDecimal {
    if (totalNumberOfSanctionsForAllOffences < 3) {
      return BigDecimal.ZERO
    }

    val lengthOfCareer =
      ageAtCurrentSanction - ageAtFirstSanction + SeriousViolentReoffendingPredictorConstant.CAREER_BOOST

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
    if (totalNumberOfViolentSanctions != 0) {
      return BigDecimal.ZERO
    }

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
  ): BigDecimal {
    if (totalNumberOfViolentSanctions != 1) {
      return BigDecimal.ZERO
    }
    return when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient
    }
  }

  fun getViolentSanctionsWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfViolentSanctions: Int,
  ): BigDecimal {
    if (totalNumberOfViolentSanctions == 0) {
      return BigDecimal.ZERO
    }

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient
    }

    return coefficient * totalNumberOfViolentSanctions.toBigDecimal()
  }

  fun getViolenceRateWeight(
    staticOrDynamic: StaticOrDynamic,
    ageAtFirstSanction: Int,
    totalNumberOfViolentSanctions: Int,
    ageAtCurrentSanction: Int,
  ): BigDecimal {
    if (totalNumberOfViolentSanctions == 0) {
      return BigDecimal.ZERO
    }

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> SeriousViolentReoffendingPredictorStatic.VIOLENT_RATE.coefficient
      StaticOrDynamic.DYNAMIC -> SeriousViolentReoffendingPredictorDynamic.VIOLENT_RATE.coefficient
    }

    val lengthOfCareer = ageAtCurrentSanction - ageAtFirstSanction + 30
    val violentSanctionsRatio = totalNumberOfViolentSanctions / lengthOfCareer.toDouble()
    val naturalLog = ln(violentSanctionsRatio)

    return naturalLog.toBigDecimal() * coefficient
  }

  fun getOffenceInvolvedCarryingOrUsingWeaponWeight(carryingOrUsingWeapon: Boolean): BigDecimal = if (carryingOrUsingWeapon) SeriousViolentReoffendingPredictorDynamic.CARRY_OR_USE_WEAPON.coefficient else BigDecimal.ZERO

  fun getSuitableAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): BigDecimal = SeriousViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient * suitabilityOfAccommodation.score.toBigDecimal()

  fun getUnemployedWeight(isUnemployed: Boolean): BigDecimal = if (isUnemployed) SeriousViolentReoffendingPredictorDynamic.UNEMPLOYED.coefficient else BigDecimal.ZERO

  fun getChronicDrinkingWeight(currentAlcoholUseProblems: ProblemLevel): BigDecimal = currentAlcoholUseProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

  fun getTemperControlWeight(temperProblems: ProblemLevel): BigDecimal = temperProblems.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.TEMPER.coefficient

  fun getProCriminalAttitudeWeight(proCriminalAttitudes: ProblemLevel): BigDecimal = proCriminalAttitudes.score.toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient

  fun getPastHomicideOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.HOMICIDE,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_HOMICIDE_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastWoundingGrievousBodilyHarmOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.WOUNDING_GBH,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_GBH_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastKidnappingOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.KIDNAPPING,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_KIDNAPPING_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastFirearmsOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.FIREARMS,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_FIREARMS_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastRobberyOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.ROBBERY,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_ROBBERY_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastAggravatedBurglaryOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.AGGRAVATED_BURGLARY,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_AGGRAVATED_BURGLARY_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastNonFirearmWeaponOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.WEAPON,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_WEAPON_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastCriminalDamageOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.CRIMINAL_DAMAGE,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_CRIMINAL_DAMAGE_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun getPastArsonOffenceWeight(previousConvictions: List<PreviousConviction>): BigDecimal = if (previousConvictions.contains(
      PreviousConviction.ARSON,
    )
  ) {
    SeriousViolentReoffendingPredictorDynamic.PAST_ARSON_OFFENCE.coefficient
  } else {
    BigDecimal.ZERO
  }

  fun calculatePercentageScore(totalWeight: BigDecimal): Double = totalWeight.toDouble().sigmoid().asDoublePercentage().sanitisePercentage()

  fun getRiskBand(percentageScore: Double): RiskBand = when {
    percentageScore <= SeriousViolentReoffendingPredictorConstant.EXCLUSIVE_MIN_PERCENTAGE -> throw IllegalArgumentException("Percentage score cannot be less than 0%: $percentageScore")

    percentageScore < SeriousViolentReoffendingPredictorConstant.MEDIUM_BAND_LOWER_BOUND -> RiskBand.LOW
    percentageScore < SeriousViolentReoffendingPredictorConstant.HIGH_BAND_LOWER_BOUND -> RiskBand.MEDIUM
    percentageScore < SeriousViolentReoffendingPredictorConstant.VERY_HIGH_BAND_LOWER_BOUND -> RiskBand.HIGH
    percentageScore < SeriousViolentReoffendingPredictorConstant.EXCLUSIVE_MAX_PERCENTAGE -> RiskBand.VERY_HIGH

    else -> throw IllegalArgumentException("Percentage score cannot exceed 100%: $percentageScore")
  }
}
