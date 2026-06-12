package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.AllReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.AllReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.AllReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import java.math.BigDecimal
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ln
import kotlin.math.pow

object AllReoffendingPredictorTransformationHelper {

  fun get2YearInterceptWeight(staticOrDynamic: StaticOrDynamic): BigDecimal = when (staticOrDynamic) {
    StaticOrDynamic.STATIC -> AllReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient
    StaticOrDynamic.DYNAMIC -> AllReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient
  }

  fun getAgeGenderPolynomialWeight(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    ageAtStartOfFollowup: Int,
  ): BigDecimal {
    val coefficients: Array<BigDecimal> = when (gender) {
      Gender.MALE -> when (staticOrDynamic) {
        StaticOrDynamic.STATIC -> arrayOf(
          BigDecimal.ZERO,
          AllReoffendingPredictorStatic.AAI_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_CUBIC_MALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUARTIC_MALE.coefficient,
        )

        StaticOrDynamic.DYNAMIC -> arrayOf(
          BigDecimal.ZERO,
          AllReoffendingPredictorDynamic.AAI_MALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_CUBIC_MALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_QUARTIC_MALE.coefficient,
        )
      }

      Gender.FEMALE -> when (staticOrDynamic) {
        StaticOrDynamic.STATIC -> arrayOf(
          BigDecimal.ZERO,
          AllReoffendingPredictorStatic.AAI_FEMALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
          AllReoffendingPredictorStatic.AAI_CUBIC_FEMALE.coefficient,
          AllReoffendingPredictorStatic.AAI_QUARTIC_FEMALE.coefficient,
        )

        StaticOrDynamic.DYNAMIC -> arrayOf(
          BigDecimal.ZERO,
          AllReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_CUBIC_FEMALE.coefficient,
          AllReoffendingPredictorDynamic.AAI_QUARTIC_FEMALE.coefficient,
        )
      }
    }

    return calculatePolynomial(coefficients, ageAtStartOfFollowup.toBigDecimal())
  }

  fun getGenderWeight(staticOrDynamic: StaticOrDynamic, gender: Gender): BigDecimal = when (gender) {
    Gender.MALE -> BigDecimal.ZERO
    Gender.FEMALE -> when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> AllReoffendingPredictorStatic.FEMALE.coefficient
      StaticOrDynamic.DYNAMIC -> AllReoffendingPredictorDynamic.FEMALE.coefficient
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
      StaticOrDynamic.STATIC -> AllReoffendingPredictorStatic.FIRST_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> AllReoffendingPredictorDynamic.FIRST_SANCTION.coefficient
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
      StaticOrDynamic.STATIC -> AllReoffendingPredictorStatic.SECOND_SANCTION.coefficient
      StaticOrDynamic.DYNAMIC -> AllReoffendingPredictorDynamic.SECOND_SANCTION.coefficient
    }
  }

  fun getTotalSanctionWeight(staticOrDynamic: StaticOrDynamic, totalNumberOfSanctionsForAllOffences: Int): BigDecimal {
    val coefficient: BigDecimal = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> AllReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient
      StaticOrDynamic.DYNAMIC -> AllReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient
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
        Gender.MALE -> AllReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> AllReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient
      }
    }

    return firstToSecondSanctionYears.toBigDecimal() * coefficient
  }

  fun getOffenceFreeMonthsPolynomialWeight(
    staticOrDynamic: StaticOrDynamic,
    assessmentDate: LocalDate,
    dateAtStartOfFollowupCalculated: LocalDate,
  ): BigDecimal {
    // They are not in the community
    if (assessmentDate.isBefore(dateAtStartOfFollowupCalculated)) {
      return BigDecimal.ZERO
    }

    val monthsBetweenAssessmentAndFollowup = ChronoUnit.MONTHS.between(
      dateAtStartOfFollowupCalculated,
      assessmentDate,
    ).coerceAtMost(36)

    val coefficients: Array<BigDecimal> = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> arrayOf(
        BigDecimal.ZERO,
        AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
        AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
        AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
        AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
      )

      StaticOrDynamic.DYNAMIC -> arrayOf(
        BigDecimal.ZERO,
        AllReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
        AllReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
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

    val lengthOfCareer = (ageAtCurrentSanction - ageAtFirstSanction) + AllReoffendingPredictor.CAREER_BOOST

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> when (gender) {
        Gender.MALE -> AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_G_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_G_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_G_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_G_FEMALE.coefficient
      }
    }

    val totalSanctionsRatio: Double = totalNumberOfSanctionsForAllOffences.toDouble() / lengthOfCareer
    val naturalLog = ln(totalSanctionsRatio)

    return naturalLog.toBigDecimal() * coefficient
  }

  fun getCopasSquaredWeight(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
  ): BigDecimal {
    if (totalNumberOfSanctionsForAllOffences < 3) {
      return BigDecimal.ZERO
    }

    val lengthOfCareer = (ageAtCurrentSanction - ageAtFirstSanction) + AllReoffendingPredictor.CAREER_BOOST

    val coefficient = when (staticOrDynamic) {
      StaticOrDynamic.STATIC -> when (gender) {
        Gender.MALE -> AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE.coefficient
      }

      StaticOrDynamic.DYNAMIC -> when (gender) {
        Gender.MALE -> AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE.coefficient
        Gender.FEMALE -> AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE.coefficient
      }
    }

    val totalSanctionsRatio: Double = totalNumberOfSanctionsForAllOffences.toDouble() / lengthOfCareer
    val naturalLog = ln(totalSanctionsRatio)

    return naturalLog.pow(2).toBigDecimal() * coefficient
  }

  fun getSuitableAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): BigDecimal = suitabilityOfAccommodation.score.toBigDecimal() * AllReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient

  fun getUnemployedWeight(isUnemployed: Boolean): BigDecimal = if (isUnemployed) AllReoffendingPredictorDynamic.UNEMPLOYED.coefficient else BigDecimal.ZERO

  fun getLiveInRelationshipWeight(currentRelationshipStatus: CurrentRelationshipStatus): BigDecimal = if (currentRelationshipStatus == CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER) AllReoffendingPredictorDynamic.LIVE_IN_RELATIONSHIP.coefficient else BigDecimal.ZERO

  fun getRelationshipQualityWeight(currentRelationshipWithPartner: ProblemLevel): BigDecimal = currentRelationshipWithPartner.score.toBigDecimal() * AllReoffendingPredictorDynamic.RELATIONSHIP_QUALITY.coefficient

  fun getMultiplicativeRelationshipWeight(
    currentRelationshipStatus: CurrentRelationshipStatus,
    currentRelationshipWithPartner: ProblemLevel,
  ): BigDecimal = if (currentRelationshipStatus == CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER && currentRelationshipWithPartner != ProblemLevel.NO_PROBLEMS) currentRelationshipWithPartner.score.toBigDecimal() * AllReoffendingPredictorDynamic.QUALITY_OF_LIVE_IN_RELATIONSHIP.coefficient else BigDecimal.ZERO

  fun getDomesticViolenceWeight(evidenceOfDomesticAbuse: Boolean): BigDecimal = if (evidenceOfDomesticAbuse) AllReoffendingPredictorDynamic.DOMESTIC_ABUSE.coefficient else BigDecimal.ZERO

  fun getRegularOffendingActivitiesWeight(regularOffendingActivities: ProblemLevel): BigDecimal = regularOffendingActivities.score.toBigDecimal() * AllReoffendingPredictorDynamic.ACTIVITIES_ENCOURAGE_OFFENDING.coefficient

  fun getDrugMotivationWeight(motivationToTackleDrugMisuse: MotivationLevel): BigDecimal = motivationToTackleDrugMisuse.score.toBigDecimal() * AllReoffendingPredictorDynamic.MOTIVATION_TO_TACKLE_DRUG_MISUSE.coefficient

  fun getChronicDrinkingWeight(currentAlcoholUseProblems: ProblemLevel): BigDecimal = currentAlcoholUseProblems.score.toBigDecimal() * AllReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

  fun getBingeDrinkingWeight(excessiveAlcoholUse: ProblemLevel): BigDecimal = excessiveAlcoholUse.score.toBigDecimal() * AllReoffendingPredictorDynamic.BINGE_DRINKING.coefficient

  fun getImpulsivityWeight(impulsivityProblems: ProblemLevel): BigDecimal = impulsivityProblems.score.toBigDecimal() * AllReoffendingPredictorDynamic.IMPULSIVITY.coefficient

  fun getProCriminalAttitudeWeight(proCriminalAttitudes: ProblemLevel): BigDecimal = proCriminalAttitudes.score.toBigDecimal() * AllReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient

  fun getHeroinUsageWeight(hasHeroinUsage: Boolean): BigDecimal = if (hasHeroinUsage) AllReoffendingPredictorDynamic.HEROIN.coefficient else BigDecimal.ZERO

  fun getOtherOpiateUsageWeight(hasOtherOpiateUsage: Boolean): BigDecimal = if (hasOtherOpiateUsage) AllReoffendingPredictorDynamic.OTHER_OPIATE.coefficient else BigDecimal.ZERO

  fun getCrackCocaineUsageWeight(hasCrackCocaineUsage: Boolean): BigDecimal = if (hasCrackCocaineUsage) AllReoffendingPredictorDynamic.CRACK_COCAINE.coefficient else BigDecimal.ZERO

  fun getPowderCocaineUsageWeight(hasPowderCocaineUsage: Boolean): BigDecimal = if (hasPowderCocaineUsage) AllReoffendingPredictorDynamic.POWDER_COCAINE.coefficient else BigDecimal.ZERO

  fun getMisusedPrescriptionDrugUsageWeight(hasMisusedPrescriptionDrugUsage: Boolean): BigDecimal = if (hasMisusedPrescriptionDrugUsage) AllReoffendingPredictorDynamic.PRESCRIPTION_DRUG_MISUSE.coefficient else BigDecimal.ZERO

  fun getBenzodiazepinesUsageWeight(hasBenzodiazepinesUsage: Boolean): BigDecimal = if (hasBenzodiazepinesUsage) AllReoffendingPredictorDynamic.BENZODIAZEPINES.coefficient else BigDecimal.ZERO

  fun getCannabisUsageWeight(hasCannabisUsage: Boolean): BigDecimal = if (hasCannabisUsage) AllReoffendingPredictorDynamic.CANNABIS.coefficient else BigDecimal.ZERO

  fun getSteroidsUsageWeight(hasSteroidsUsage: Boolean): BigDecimal = if (hasSteroidsUsage) AllReoffendingPredictorDynamic.STEROIDS.coefficient else BigDecimal.ZERO

  fun getOtherDrugsUsageWeight(
    hasOtherDrugsUsage: Boolean,
    hasKetamineUsage: Boolean,
    hasSpiceUsage: Boolean,
    hasHallucinogensUsage: Boolean,
    hasSolventsUsage: Boolean,
  ): BigDecimal = if (hasOtherDrugsUsage || hasKetamineUsage || hasSpiceUsage || hasHallucinogensUsage || hasSolventsUsage) AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient else BigDecimal.ZERO

  fun calculatePercentageScore(totalWeight: BigDecimal): Double = totalWeight.toDouble().sigmoid().asDoublePercentage().sanitisePercentage()

  fun getRiskBand(percentageScore: Double): RiskBand = when {
    percentageScore <= AllReoffendingPredictor.EXCLUSIVE_MIN_PERCENTAGE -> throw IllegalArgumentException("Percentage score cannot be less than 0%: $percentageScore")

    percentageScore < AllReoffendingPredictor.MEDIUM_BAND_LOWER_BOUND -> RiskBand.LOW
    percentageScore < AllReoffendingPredictor.HIGH_BAND_LOWER_BOUND -> RiskBand.MEDIUM
    percentageScore < AllReoffendingPredictor.VERY_HIGH_BAND_LOWER_BOUND -> RiskBand.HIGH
    percentageScore < AllReoffendingPredictor.EXCLUSIVE_MAX_PERCENTAGE -> RiskBand.VERY_HIGH

    else -> throw IllegalArgumentException("Percentage score cannot exceed 100%: $percentageScore")
  }
}
