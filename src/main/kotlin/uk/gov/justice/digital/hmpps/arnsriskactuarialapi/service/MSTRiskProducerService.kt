package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMaturityFlag
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMstApplicable
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.genderAndAgeValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.mstInitialValidation

@Service
class MSTRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = mstInitialValidation(request)

    if (!errors.isEmpty()) {
      return context.copy(
        MST = MSTObject(
          null,
          null,
          null,
          errors,
        ),
      )
    }

    val validRequest = MSTRequestValidated(
      gender = request.gender!!,
      assessmentDate = request.assessmentDate,
      dateOfBirth = request.dateOfBirth!!,
      peerGroupInfluences = request.peerGroupInfluences!!,
      attitudesPeerPressure = request.attitudesPeerPressure!!,
      attitudesStableBehaviour = request.attitudesStableBehaviour!!,
      difficultiesCoping = request.difficultiesCoping!!,
      attitudesTowardsSelf = request.attitudesTowardsSelf!!,
      impulsivityBehaviour = request.impulsivityBehaviour!!,
      temperControl = request.temperControl!!,
      problemSolvingSkills = request.problemSolvingSkills!!,
      awarenessOfConsequences = request.awarenessOfConsequences!!,
      understandsPeoplesViews = request.understandsPeoplesViews!!,
    )

    return context.copy(
      MST = getMstObject(validRequest, errors),
    )
  }

  private fun getMstObject(
    request: MSTRequestValidated,
    errors: MutableList<ValidationErrorResponse>,
  ): MSTObject {
    val currentAge = calculateAge(request.dateOfBirth, request.assessmentDate)
    val isMstApplicable = getMstApplicable(request.gender, currentAge)

    if (isMstApplicable) {
      val maturityScore = listOf(
        if (request.peerGroupInfluences) 1 else 0,
        request.attitudesPeerPressure.score,
        request.attitudesStableBehaviour.score,
        request.difficultiesCoping.score,
        request.attitudesTowardsSelf.score,
        request.impulsivityBehaviour.score,
        request.temperControl.score,
        request.problemSolvingSkills.score,
        request.awarenessOfConsequences.score,
        request.understandsPeoplesViews.score,
      ).sum()

      return MSTObject(
        maturityScore,
        getMaturityFlag(maturityScore),
        true,
        errors,
      )
    }

    return MSTObject(
      null,
      null,
      false,
      genderAndAgeValidation(request.gender, currentAge, errors),
    )
  }
}
