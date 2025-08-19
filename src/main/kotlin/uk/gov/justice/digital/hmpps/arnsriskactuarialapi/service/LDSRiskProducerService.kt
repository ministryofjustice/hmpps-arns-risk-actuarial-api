package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.hasProblemsWithNumeracyOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.hasProblemsWithReadingOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.ldsScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.ldsSubTotal
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.learningDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.problemsWithReadingWritingNumeracyOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.professionalOrVocationalQualificationsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.workRelatedSkillsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ldsInitialValidation

@Service
class LDSRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ldsInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply { LDS = LDSObject(null, errors) }
    }

    val validInput = LDSInputValidated(
      request.isCurrentlyOfNoFixedAbodeOrTransientAccommodation,
      request.workRelatedSkills,
      request.problemsWithReadingWritingNumeracy,
      request.hasProblemsWithReading,
      request.hasProblemsWithNumeracy,
      request.learningDifficulties,
      request.professionalOrVocationalQualifications,
    )

    return context.apply { LDS = getLDSOutput(validInput) }
  }

  companion object {

    fun getLDSOutput(input: LDSInputValidated): LDSObject {
      // Transformation Steps
      val isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS =
        isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS(input.isCurrentlyOfNoFixedAbodeOrTransientAccommodation)
      val workRelatedSkillsOffendersScore =
        workRelatedSkillsOffendersScore(input.workRelatedSkills)
      val problemsWithReadingWritingNumeracyOffendersScore =
        problemsWithReadingWritingNumeracyOffendersScore(input.problemsWithReadingWritingNumeracy)
      val hasProblemsWithReadingOffendersScore =
        hasProblemsWithReadingOffendersScore(input.hasProblemsWithReading)
      val hasProblemsWithNumeracyOffendersScore =
        hasProblemsWithNumeracyOffendersScore(input.hasProblemsWithNumeracy)
      val learningDifficultiesOffendersScore =
        learningDifficultiesOffendersScore(input.learningDifficulties)
      val professionalOrVocationalQualificationsOffendersScore =
        professionalOrVocationalQualificationsOffendersScore(input.professionalOrVocationalQualifications)
      val ldsSubTotal = ldsSubTotal(
        isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScoreLDS,
        workRelatedSkillsOffendersScore,
        problemsWithReadingWritingNumeracyOffendersScore,
        hasProblemsWithReadingOffendersScore,
        hasProblemsWithNumeracyOffendersScore,
        learningDifficultiesOffendersScore,
        professionalOrVocationalQualificationsOffendersScore,
      )

      // Create LDS Output
      val ldsScore = ldsScore(ldsSubTotal)
      return LDSObject(ldsScore, emptyList())
    }
  }
}
