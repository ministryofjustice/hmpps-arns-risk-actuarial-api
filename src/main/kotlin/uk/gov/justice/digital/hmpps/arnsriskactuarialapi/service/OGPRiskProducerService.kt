package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp.OGPObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.awarenessOfConsequencesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.bandOGP
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.currentAccommodationOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.currentAccommodationWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.currentDrugMisuseOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.drugMisuseNonViolentOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.drugMisuseNonViolentWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.employmentStatusOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.employmentStatusWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.motivationDrugOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.ogpReoffendingOneYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.ogpReoffendingTwoYear
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.ogrs3TwoYearWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.proCriminalAttitudesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.proCriminalAttitudesWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.problemSolvingSkillsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.regularOffendingActivitiesOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.regularOffendingActivitiesWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.thinkingAndBehaviourNonViolentOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.thinkingAndBehaviourNonViolentWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.totalOGPScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.understandsPeoplesViewsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.OGPValidationHelper.Companion.ogpInitialValidation

@Service
class OGPRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = ogpInitialValidation(request, context)

    if (errors.isNotEmpty()) {
      return context.apply {
        OGP = OGPObject(null, null, null, null, errors)
      }
    }

    val validInput = OGPInputValidated(
      context.OGRS3?.ogrs3TwoYear!!,
      request.currentAccommodation!!,
      request.employmentStatus!!,
      request.regularOffendingActivities!!,
      request.currentDrugMisuse!!,
      request.motivationDrug!!,
      request.problemSolvingSkills!!,
      request.awarenessOfConsequences!!,
      request.understandsPeoplesViews!!,
      request.proCriminalAttitudes!!,
    )

    return context.apply { OGP = getOGPOutput(validInput) }
  }

  companion object {
    fun getOGPOutput(input: OGPInputValidated): OGPObject = runCatching {
      // Transformation Step
      val currentAccommodationOffendersScore =
        currentAccommodationOffendersScore(input.currentAccommodation)
      val employmentStatusOffendersScore =
        employmentStatusOffendersScore(input.employmentStatus)
      val regularOffendingActivitiesOffendersScore =
        regularOffendingActivitiesOffendersScore(input.regularOffendingActivities)
      val currentDrugMisuseOffendersScore =
        currentDrugMisuseOffendersScore(input.currentDrugMisuse)
      val motivationDrugOffendersScore =
        motivationDrugOffendersScore(input.motivationDrug)
      val problemSolvingSkillsOffendersScore =
        problemSolvingSkillsOffendersScore(input.problemSolvingSkills)
      val awarenessOfConsequencesOffendersScore =
        awarenessOfConsequencesOffendersScore(input.awarenessOfConsequences)
      val understandsPeoplesViewsOffendersScore =
        understandsPeoplesViewsOffendersScore(input.understandsPeoplesViews)
      val proCriminalAttitudesOffendersScore =
        proCriminalAttitudesOffendersScore(input.proCriminalAttitudes)
      val drugMisuseNonViolentOffendersScore =
        drugMisuseNonViolentOffendersScore(currentDrugMisuseOffendersScore, motivationDrugOffendersScore)
      val thinkingAndBehaviourNonViolentOffendersScore =
        thinkingAndBehaviourNonViolentOffendersScore(
          problemSolvingSkillsOffendersScore,
          awarenessOfConsequencesOffendersScore,
          understandsPeoplesViewsOffendersScore,
        )
      // Weighted Scores
      val ogrs3TwoYearWeighted = ogrs3TwoYearWeighted(input.ogrs3TwoYear)
      val currentAccommodationWeighted =
        currentAccommodationWeighted(currentAccommodationOffendersScore)
      val employmentStatusWeighted =
        employmentStatusWeighted(employmentStatusOffendersScore)
      val regularOffendingActivitiesWeighted =
        regularOffendingActivitiesWeighted(regularOffendingActivitiesOffendersScore)
      val drugMisuseNonViolentWeighted = drugMisuseNonViolentWeighted(drugMisuseNonViolentOffendersScore)
      val thinkingAndBehaviourNonViolentWeighted =
        thinkingAndBehaviourNonViolentWeighted(thinkingAndBehaviourNonViolentOffendersScore)
      val proCriminalAttitudesWeighted =
        proCriminalAttitudesWeighted(proCriminalAttitudesOffendersScore)
      // Final Outputs
      val totalOGPScore =
        totalOGPScore(
          ogrs3TwoYearWeighted,
          currentAccommodationWeighted,
          employmentStatusWeighted,
          regularOffendingActivitiesWeighted,
          drugMisuseNonViolentWeighted,
          thinkingAndBehaviourNonViolentWeighted,
          proCriminalAttitudesWeighted,
        )
      val ogpReoffendingOneYear = ogpReoffendingOneYear(totalOGPScore)
      val ogpReoffendingTwoYear = ogpReoffendingTwoYear(totalOGPScore)
      val bandOGP = bandOGP(ogpReoffendingTwoYear)
      // Create OGP Output
      OGPObject(ogpReoffendingOneYear, ogpReoffendingTwoYear, bandOGP, totalOGPScore, emptyList())
    }.getOrElse {
      // Create OGP Output
      OGPObject(
        null,
        null,
        null,
        null,
        listOf(
          ValidationErrorResponse(
            type = ValidationErrorType.UNEXPECTED_VALUE,
            message = "Error: ${it.message}",
            fields = null,
          ),
        ),
      )
    }
  }
}
