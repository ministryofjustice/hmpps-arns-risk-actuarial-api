package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMaturityFlag
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMstApplicable
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.mstInitialValidation

@Service
class MSTRiskProducerService : RiskProducer<MSTObject> {

  override fun getRiskScore(riskScoreRequest: RiskScoreRequest): MSTObject {
    val errors = mstInitialValidation(riskScoreRequest)

    if (!errors.isEmpty()) {
      return MSTObject(
        riskScoreRequest.version,
        null,
        null,
        null,
        errors,
      )
    }

    val validRequest = MSTRequestValidated(
      riskScoreRequest.version,
      riskScoreRequest.gender!!,
      riskScoreRequest.dateOfBirth!!,
      riskScoreRequest.peerGroupInfluences!!,
      riskScoreRequest.attitudesPeerPressure!!,
      riskScoreRequest.attitudesStableBehaviour!!,
      riskScoreRequest.difficultiesCoping!!,
      riskScoreRequest.attitudesTowardsSelf!!,
      riskScoreRequest.impusilvityBehaviour!!,
      riskScoreRequest.temperControl!!,
      riskScoreRequest.problemSolvingSkills!!,
      riskScoreRequest.awarenessOfConsequences!!,
      riskScoreRequest.understandsPeoplesViews!!,
    )

    return getMstObject(validRequest, errors)
  }

  private fun getMstObject(
    request: MSTRequestValidated,
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
        request.impusilvityBehaviour.score,
        request.temperControl.score,
        request.problemSolvingSkills.score,
        request.awarenessOfConsequences.score,
        request.understandsPeoplesViews.score,
      ).sum()

      return MSTObject(
        request.version,
        maturityScore,
        getMaturityFlag(maturityScore),
        true,
        errors,
      )
    }

    errors.add(
      ValidationErrorResponse(
        type = ValidationErrorType.NOT_APPLICABLE,
        message = "ERR - Does not meet eligibility criteria",
        fields = listOf("Gender", "Date of birth"),
      ),
    )
    return MSTObject(
      request.version,
      null,
      null,
      false,
      errors,
    )
  }
}
