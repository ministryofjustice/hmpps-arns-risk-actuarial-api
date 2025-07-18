package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MstInput
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MstObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MstRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.calculateAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMaturityFlag
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getMstApplicable
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.mstInitialValidation

@Service
class MstScoreProducerService {

  fun getMstScore(mstInput: MstInput): MstObject {
    val errors = mstInitialValidation(mstInput)

    if (!errors.isEmpty()) {
      return MstObject(
        mstInput.version,
        null,
        null,
        null,
        errors,
      )
    }

    val validRequest = MstRequestValidated(
      mstInput.version,
      mstInput.gender!!,
      mstInput.dateOfBirth!!,
      mstInput.peerGroupInfluences!!,
      mstInput.attitudesPeerPressure!!,
      mstInput.attitudesStableBehaviour!!,
      mstInput.difficultiesCoping!!,
      mstInput.attitudesTowardsSelf!!,
      mstInput.impusilvityBehaviour!!,
      mstInput.temperControl!!,
      mstInput.problemSolvingSkills!!,
      mstInput.awarenessOfConsequences!!,
      mstInput.understandsPeoplesViews!!,
    )

    return getMstObject(validRequest, errors)
  }

  private fun getMstObject(
    request: MstRequestValidated,
    errors: MutableList<ValidationErrorResponse>,
  ): MstObject {
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

      return MstObject(
        request.version,
        maturityScore,
        getMaturityFlag(maturityScore),
        true,
        errors,
      )
    } else {
      errors.add(
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = "ERR - Does not meet eligibility criteria",
          fields = listOf("Gender", "Date of birth"),
        ),
      )

      return MstObject(
        request.version,
        null,
        null,
        null,
        errors,
      )
    }
  }
}
