package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeCacheService
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.getViolentReoffendingPredictorDynamicOffenceCodeCoefficient
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.getViolentReoffendingPredictorStaticOffenceCodeCoefficient
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.ViolentReoffendingPredictorConstant
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ln

object ViolentReoffendingPredictorTransformationHelper {

  fun get2YearInterceptWeight(staticOrDynamic: StaticOrDynamic): BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient
    StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient
  }

  fun getAgeGenderPolynomialWeight(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    ageAtStartOfFollowup: Int,
  ): BigDecimal {
    val coefficients: Array<BigDecimal> = when (gender) {
      Gender.MALE -> when (staticOrDynamic) {
        StaticOrDynamic.STATIC -> arrayOf(
          ViolentReoffendingPredictorStatic.AAI_MALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_CUBIC_MALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_QUARTIC_MALE.coefficient,
        )

        StaticOrDynamic.DYNAMIC -> arrayOf(
          ViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
          ViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
        )
      }

      Gender.FEMALE -> when (staticOrDynamic) {
        StaticOrDynamic.STATIC -> arrayOf(
          ViolentReoffendingPredictorStatic.AAI_FEMALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_CUBIC_FEMALE.coefficient,
          ViolentReoffendingPredictorStatic.AAI_QUARTIC_FEMALE.coefficient,
        )

        StaticOrDynamic.DYNAMIC -> arrayOf(
          ViolentReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
          ViolentReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
          ViolentReoffendingPredictorDynamic.AAI_CUBIC_FEMALE.coefficient,
          ViolentReoffendingPredictorDynamic.AAI_QUARTIC_FEMALE.coefficient,
        )
      }
    }

    return calculatePolynomial(coefficients, ageAtStartOfFollowup.toBigDecimal())
  }

  fun getGenderWeight(staticOrDynamic: StaticOrDynamic, gender: Gender): BigDecimal = when (gender) {
    Gender.MALE -> BigDecimal.ZERO
    Gender.FEMALE -> when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.FEMALE.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.FEMALE.coefficient
    }
  }

  fun getOffenceGroupWeight(offenceCodeCacheService: OffenceCodeCacheService, staticOrDynamic: StaticOrDynamic, currentOffenceCode: String): BigDecimal {
    // The currentOffenceCode has been prevalidated but just in case throw an error if this returns null
    val actuarialCategory = offenceCodeCacheService.getActuarialCategory(currentOffenceCode)
      ?: throw IllegalArgumentException("Offence code mapping for $currentOffenceCode not found, ensure this is validated before the calculation")
    return when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> getViolentReoffendingPredictorStaticOffenceCodeCoefficient(actuarialCategory, currentOffenceCode)
      StaticOrDynamic.DYNAMIC -> getViolentReoffendingPredictorDynamicOffenceCodeCoefficient(actuarialCategory, currentOffenceCode)
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
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient
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
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient
    }
  }

  fun getTotalSanctionWeight(staticOrDynamic: StaticOrDynamic, totalNumberOfSanctionsForAllOffences: Int): BigDecimal {
    val coefficient: BigDecimal = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient
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
        Gender.MALE -> ViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> ViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
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
    if (assessmentDate.isBefore(dateAtStartOfFollowup)) return BigDecimal.ZERO

    val monthsBetweenAssessmentAndFollowup = ChronoUnit.MONTHS.between(
      dateAtStartOfFollowup,
      assessmentDate,
    ).coerceAtMost(36)

    val coefficients: Array<BigDecimal> = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> arrayOf(
        ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
        ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
        ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
        ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
      )

      StaticOrDynamic.DYNAMIC -> arrayOf(
        ViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
        ViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
      )
    }

    return calculatePolynomial(coefficients, monthsBetweenAssessmentAndFollowup.toBigDecimal())
  }

  fun getCopasVWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
  ): BigDecimal {
    if (totalNumberOfSanctionsForAllOffences < 3) {
      return BigDecimal.ZERO
    }

    val lengthOfCareer = (ageAtCurrentSanction - ageAtFirstSanction) + ViolentReoffendingPredictorConstant.CAREER_BOOST_COPAS

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> when (gender) {
        Gender.MALE -> ViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> ViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient
      }
    }

    val totalSanctionsRatio: Double = totalNumberOfSanctionsForAllOffences.toDouble() / lengthOfCareer
    val naturalLog = ln(totalSanctionsRatio)

    return naturalLog.toBigDecimal() * coefficient
  }

  fun getCopasViolentOffencesWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfViolentSanctions: Int,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
  ): BigDecimal {
    if (totalNumberOfViolentSanctions == 0) {
      return BigDecimal.ZERO
    }

    val lengthOfCareer = (ageAtCurrentSanction - ageAtFirstSanction) + ViolentReoffendingPredictorConstant.CAREER_BOOST_VIOLENT

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.VIOLENT_RATE.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.VIOLENT_RATE.coefficient
    }

    val totalSanctionsRatio: Double = totalNumberOfViolentSanctions.toDouble() / lengthOfCareer
    val naturalLog = ln(totalSanctionsRatio)

    return naturalLog.toBigDecimal() * coefficient
  }

  fun getNeverViolentWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForViolentOffences: Int,
    gender: Gender,
  ): BigDecimal = if (totalNumberOfSanctionsForViolentOffences > 0) {
    BigDecimal.ZERO
  } else {
    when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> when (gender) {
        Gender.MALE -> ViolentReoffendingPredictorStatic.NEVER_VIOLENT_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorStatic.NEVER_VIOLENT_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> ViolentReoffendingPredictorDynamic.NEVER_VIOLENT_MALE.coefficient
        Gender.FEMALE -> ViolentReoffendingPredictorDynamic.NEVER_VIOLENT_FEMALE.coefficient
      }
    }
  }

  fun getOnceViolentWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForViolentOffences: Int,
  ): BigDecimal = if (totalNumberOfSanctionsForViolentOffences != 1) {
    BigDecimal.ZERO
  } else {
    when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient
      StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient
    }
  }

  fun getTotalViolentSanctionsWeight(staticOrDynamic: StaticOrDynamic, totalNumberOfViolentSanctions: Int): BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> ViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient
    StaticOrDynamic.DYNAMIC -> ViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient
  }.times(BigDecimal(totalNumberOfViolentSanctions))

  fun getSuitableAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): BigDecimal = suitabilityOfAccommodation.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient

  fun getUnemployedWeight(isUnemployed: Boolean): BigDecimal = if (isUnemployed) ViolentReoffendingPredictorDynamic.UNEMPLOYED.coefficient * BigDecimal.TWO else BigDecimal.ZERO

  fun getLiveInRelationshipWeight(currentRelationshipStatus: CurrentRelationshipStatus): BigDecimal = if (currentRelationshipStatus == CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER) ViolentReoffendingPredictorDynamic.LIVE_IN_RELATIONSHIP.coefficient else BigDecimal.ZERO

  fun getRelationshipQualityWeight(currentRelationshipWithPartner: ProblemLevel): BigDecimal = currentRelationshipWithPartner.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.RELATIONSHIP_QUALITY.coefficient

  fun getMultiplicativeRelationshipWeight(
    currentRelationshipStatus: CurrentRelationshipStatus,
    currentRelationshipWithPartner: ProblemLevel,
  ): BigDecimal = if (currentRelationshipStatus == CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER && currentRelationshipWithPartner != ProblemLevel.NO_PROBLEMS) currentRelationshipWithPartner.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.QUALITY_OF_LIVE_IN_RELATIONSHIP.coefficient else BigDecimal.ZERO

  fun getDomesticViolenceWeight(evidenceOfDomesticAbuse: Boolean): BigDecimal = if (evidenceOfDomesticAbuse) ViolentReoffendingPredictorDynamic.DOMESTIC_ABUSE.coefficient else BigDecimal.ZERO

  fun getRegularOffendingActivitiesWeight(regularOffendingActivities: ProblemLevel): BigDecimal = regularOffendingActivities.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.ACTIVITIES_ENCOURAGE_OFFENDING.coefficient

  fun getDrugMotivationWeight(motivationToTackleDrugMisuse: MotivationLevel): BigDecimal = motivationToTackleDrugMisuse.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.MOTIVATION_TO_TACKLE_DRUG_MISUSE.coefficient

  fun getChronicDrinkingWeight(currentAlcoholUseProblems: ProblemLevel): BigDecimal = currentAlcoholUseProblems.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

  fun getBingeDrinkingWeight(excessiveAlcoholUse: ProblemLevel): BigDecimal = excessiveAlcoholUse.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.BINGE_DRINKING.coefficient

  fun getImpulsivityWeight(impulsivityProblems: ProblemLevel): BigDecimal = impulsivityProblems.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.IMPULSIVITY.coefficient

  fun getTemperControlWeight(temperProblems: ProblemLevel): BigDecimal = temperProblems.score.toBigDecimal() * ViolentReoffendingPredictorDynamic.TEMPER.coefficient

  fun getMethadoneUsageWeight(hasMethadoneUsage: Boolean): BigDecimal = if (hasMethadoneUsage) ViolentReoffendingPredictorDynamic.METHADONE.coefficient else BigDecimal.ZERO

  fun getOtherOpiateUsageWeight(hasOtherOpiateUsage: Boolean): BigDecimal = if (hasOtherOpiateUsage) ViolentReoffendingPredictorDynamic.OTHER_OPIATE.coefficient else BigDecimal.ZERO

  fun getCrackCocaineUsageWeight(hasCrackCocaineUsage: Boolean): BigDecimal = if (hasCrackCocaineUsage) ViolentReoffendingPredictorDynamic.CRACK_COCAINE.coefficient else BigDecimal.ZERO

  fun getPowderCocaineUsageWeight(hasPowderCocaineUsage: Boolean): BigDecimal = if (hasPowderCocaineUsage) ViolentReoffendingPredictorDynamic.POWDER_COCAINE.coefficient else BigDecimal.ZERO

  fun getMisusedPrescriptionDrugUsageWeight(hasMisusedPrescriptionDrugUsage: Boolean): BigDecimal = if (hasMisusedPrescriptionDrugUsage) ViolentReoffendingPredictorDynamic.PRESCRIPTION_DRUG_MISUSE.coefficient else BigDecimal.ZERO

  fun getBenzodiazepinesUsageWeight(hasBenzodiazepinesUsage: Boolean): BigDecimal = if (hasBenzodiazepinesUsage) ViolentReoffendingPredictorDynamic.BENZODIAZEPINES.coefficient else BigDecimal.ZERO

  fun getCannabisUsageWeight(hasCannabisUsage: Boolean): BigDecimal = if (hasCannabisUsage) ViolentReoffendingPredictorDynamic.CANNABIS.coefficient else BigDecimal.ZERO

  fun getSteroidsUsageWeight(hasSteroidsUsage: Boolean): BigDecimal = if (hasSteroidsUsage) ViolentReoffendingPredictorDynamic.STEROIDS.coefficient else BigDecimal.ZERO

  fun getOtherDrugsUsageWeight(
    hasOtherDrugsUsage: Boolean,
    hasKetamineUsage: Boolean,
    hasSpiceUsage: Boolean,
    hasHallucinogensUsage: Boolean,
    hasSolventsUsage: Boolean,
  ): BigDecimal = if (hasOtherDrugsUsage || hasKetamineUsage || hasSpiceUsage || hasHallucinogensUsage || hasSolventsUsage) ViolentReoffendingPredictorDynamic.OTHER_DRUGS.coefficient else BigDecimal.ZERO

  fun calculatePercentageScore(totalWeight: BigDecimal): Double = totalWeight.toDouble().sigmoid().asDoublePercentage().sanitisePercentage()

  fun getRiskBand(percentageScore: Double): RiskBand = when {
    percentageScore <= ViolentReoffendingPredictorConstant.EXCLUSIVE_MIN_PERCENTAGE -> throw IllegalArgumentException(
      "Percentage score cannot be less than 0%: $percentageScore",
    )

    percentageScore < ViolentReoffendingPredictorConstant.MEDIUM_BAND_LOWER_BOUND -> RiskBand.LOW
    percentageScore < ViolentReoffendingPredictorConstant.HIGH_BAND_LOWER_BOUND -> RiskBand.MEDIUM
    percentageScore < ViolentReoffendingPredictorConstant.VERY_HIGH_BAND_LOWER_BOUND -> RiskBand.HIGH
    percentageScore < ViolentReoffendingPredictorConstant.EXCLUSIVE_MAX_PERCENTAGE -> RiskBand.VERY_HIGH

    else -> throw IllegalArgumentException("Percentage score cannot exceed 100%: $percentageScore")
  }
}
