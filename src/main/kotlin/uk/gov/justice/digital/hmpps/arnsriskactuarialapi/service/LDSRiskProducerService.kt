package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.currentAccommodationOffendersScoreLDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.educationDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.ldsScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.ldsSubTotal
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.learningDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.numeracyDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.professionalOrVocationalQualificationsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.readingDifficultiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.LDSTransformationHelper.Companion.transferableSkillsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ldsInitialValidation

@Service
class LDSRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ldsInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply { LDS = LDSObject(null, errors) }
    }

    val validInput = LDSInputValidated(
      request.currentAccommodation,
      request.transferableSkills,
      request.educationDifficulties,
      request.readingDifficulties,
      request.numeracyDifficulties,
      request.learningDifficulties,
      request.professionalOrVocationalQualifications,
    )

    return context.apply { LDS = getLDSOutput(validInput) }
  }

  companion object {

    fun getLDSOutput(input: LDSInputValidated): LDSObject {
      // Transformation Steps
      val currentAccommodationOffendersScoreLDS =
        currentAccommodationOffendersScoreLDS(input.currentAccommodation)
      val transferableSkillsOffendersScore =
        transferableSkillsOffendersScore(input.transferableSkills)
      val educationDifficultiesOffendersScore =
        educationDifficultiesOffendersScore(input.educationDifficulties)
      val readingDifficultiesOffendersScore =
        readingDifficultiesOffendersScore(input.readingDifficulties)
      val numeracyDifficultiesOffendersScore =
        numeracyDifficultiesOffendersScore(input.numeracyDifficulties)
      val learningDifficultiesOffendersScore =
        learningDifficultiesOffendersScore(input.learningDifficulties)
      val professionalOrVocationalQualificationsOffendersScore =
        professionalOrVocationalQualificationsOffendersScore(input.professionalOrVocationalQualifications)
      val ldsSubTotal = ldsSubTotal(
        currentAccommodationOffendersScoreLDS,
        transferableSkillsOffendersScore,
        educationDifficultiesOffendersScore,
        readingDifficultiesOffendersScore,
        numeracyDifficultiesOffendersScore,
        learningDifficultiesOffendersScore,
        professionalOrVocationalQualificationsOffendersScore,
      )

      // Create LDS Output
      val ldsScore = ldsScore(ldsSubTotal)
      return LDSObject(ldsScore, emptyList())
    }
  }
}
