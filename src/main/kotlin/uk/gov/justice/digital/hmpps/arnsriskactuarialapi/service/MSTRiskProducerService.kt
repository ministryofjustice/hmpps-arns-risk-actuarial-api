package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MSTVersion
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
    val algorithmVersion = request.version.mstVersion
    val errors = mstInitialValidation(request)

    if (!errors.isEmpty()) {
      return context.copy(
        MST = MSTObject(
          algorithmVersion,
          null,
          null,
          null,
          errors,
        ),
      )
    }

    val validRequest = MSTRequestValidated(
      request.gender!!,
      request.dateOfBirth!!,
      request.peerGroupInfluences!!,
      request.attitudesPeerPressure!!,
      request.attitudesStableBehaviour!!,
      request.difficultiesCoping!!,
      request.attitudesTowardsSelf!!,
      request.impulsivityBehaviour!!,
      request.temperControl!!,
      request.problemSolvingSkills!!,
      request.awarenessOfConsequences!!,
      request.understandsPeoplesViews!!,
    )

    return context.copy(
      MST = getMstObject(validRequest, algorithmVersion, errors),
    )
  }

  private fun getMstObject(
    request: MSTRequestValidated,
    algorithmVersion: MSTVersion,
    errors: MutableList<ValidationErrorResponse>,
  ): MSTObject {
    val currentAge = calculateAge(request.dateOfBirth)
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
        algorithmVersion,
        maturityScore,
        getMaturityFlag(maturityScore),
        true,
        errors,
      )
    }

    return MSTObject(
      algorithmVersion,
      null,
      null,
      false,
      genderAndAgeValidation(request.gender, currentAge, errors),
    )
  }
}
