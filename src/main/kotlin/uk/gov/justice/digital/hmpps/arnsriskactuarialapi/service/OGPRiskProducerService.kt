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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.currentDrugMisuseOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.drugMisuseNonViolentOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.drugMisuseNonViolentWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.isUnemployedOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.isUnemployedWeighted
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.motivationToTackleDrugMisuseOffendersScore
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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OGPTransformationHelper.Companion.understandsOtherPeoplesViewsOffendersScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateOGP

@Service
class OGPRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = validateOGP(request, context)

    if (errors.isNotEmpty()) {
      return context.apply {
        OGP = OGPObject(null, null, null, null, errors)
      }
    }

    val validInput = OGPInputValidated(
      context.OGRS3?.ogrs3TwoYear!!,
      request.isCurrentlyOfNoFixedAbodeOrTransientAccommodation!!,
      request.isUnemployed!!,
      request.regularOffendingActivities!!,
      request.currentDrugMisuse!!,
      request.motivationToTackleDrugMisuse!!,
      request.problemSolvingSkills!!,
      request.awarenessOfConsequences!!,
      request.understandsOtherPeoplesViews!!,
      request.proCriminalAttitudes!!,
    )

    return context.apply { OGP = getOGPOutput(validInput) }
  }

  companion object {
    fun getOGPOutput(input: OGPInputValidated): OGPObject = runCatching {
      // Transformation Step
      val isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore =
        isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore(input.isCurrentlyOfNoFixedAbodeOrTransientAccommodation)
      val isUnemployedOffendersScore =
        isUnemployedOffendersScore(input.isUnemployed)
      val regularOffendingActivitiesOffendersScore =
        regularOffendingActivitiesOffendersScore(input.regularOffendingActivities)
      val currentDrugMisuseOffendersScore =
        currentDrugMisuseOffendersScore(input.currentDrugMisuse)
      val motivationToTackleDrugMisuseOffendersScore =
        motivationToTackleDrugMisuseOffendersScore(input.motivationToTackleDrugMisuse)
      val problemSolvingSkillsOffendersScore =
        problemSolvingSkillsOffendersScore(input.problemSolvingSkills)
      val awarenessOfConsequencesOffendersScore =
        awarenessOfConsequencesOffendersScore(input.awarenessOfConsequences)
      val understandsOtherPeoplesViewsOffendersScore =
        understandsOtherPeoplesViewsOffendersScore(input.understandsOtherPeoplesViews)
      val proCriminalAttitudesOffendersScore =
        proCriminalAttitudesOffendersScore(input.proCriminalAttitudes)
      val drugMisuseNonViolentOffendersScore =
        drugMisuseNonViolentOffendersScore(currentDrugMisuseOffendersScore, motivationToTackleDrugMisuseOffendersScore)
      val thinkingAndBehaviourNonViolentOffendersScore =
        thinkingAndBehaviourNonViolentOffendersScore(
          problemSolvingSkillsOffendersScore,
          awarenessOfConsequencesOffendersScore,
          understandsOtherPeoplesViewsOffendersScore,
        )
      // Weighted Scores
      val ogrs3TwoYearWeighted = ogrs3TwoYearWeighted(input.ogrs3TwoYear)
      val isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted =
        isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted(isCurrentlyOfNoFixedAbodeOrTransientAccommodationOffendersScore)
      val isUnemployedWeighted =
        isUnemployedWeighted(isUnemployedOffendersScore)
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
          isCurrentlyOfNoFixedAbodeOrTransientAccommodationWeighted,
          isUnemployedWeighted,
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
            fields = emptyList(),
          ),
        ),
      )
    }
  }
}
