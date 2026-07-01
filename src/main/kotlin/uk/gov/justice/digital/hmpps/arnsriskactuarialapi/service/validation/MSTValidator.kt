package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

@Component
class MSTValidator {
  val minMstAnswersSize = 9

  val mstRequiredFields = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::dateOfBirth,
  )

  val mstMinimumAnswers = listOf(
    RiskScoreRequest::hasPeerGroupInfluences,
    RiskScoreRequest::influenceFromCriminalAssociates,
    RiskScoreRequest::recklessnessAndRiskTakingBehaviour,
    RiskScoreRequest::difficultiesCoping,
    RiskScoreRequest::attitudesTowardsSelf,
    RiskScoreRequest::impulsivityProblems,
    RiskScoreRequest::temperControl,
    RiskScoreRequest::problemSolvingSkills,
    RiskScoreRequest::awarenessOfConsequences,
    RiskScoreRequest::understandsOtherPeoplesViews,
  )

  fun validateMST(request: RiskScoreRequest): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()
    validateRequiredFields(request, errors)
    return errors
  }

  private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationError>) {
    val missingFields = arrayListOf<String>()

    mstRequiredFields.forEach { missingFields.addIfNull(request, it) }

    val actualNonNullAnswers = mstMinimumAnswers.mapNotNull { wib -> wib.get(request) }.size
    if (actualNonNullAnswers < minMstAnswersSize) {
      mstMinimumAnswers.forEach { missingFields.addIfNull(request, it) }
    }

    if (missingFields.isNotEmpty()) {
      errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asError(missingFields)
    }
  }

  fun getMstApplicable(gender: Gender, age: Int): Boolean = isValidMstGender(gender) && isValidMstAge(age)

  fun isValidMstAge(age: Int): Boolean = age in 18..25

  fun isValidMstGender(gender: Gender): Boolean = gender == Gender.MALE

  fun isNotNullAndInvalidMstAge(age: Int?): Boolean = age != null && !isValidMstAge(age)
}
