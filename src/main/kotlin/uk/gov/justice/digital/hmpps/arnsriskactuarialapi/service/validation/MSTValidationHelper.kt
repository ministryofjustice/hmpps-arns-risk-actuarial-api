package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

const val MIN_MST_ANSWERS_SIZE = 9

val MST_REQUIRED_FIELDS = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
)

val MST_MINIMUM_ANSWERS = listOf(
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

fun validateMST(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  MST_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }

  val actualNonNullAnswers = MST_MINIMUM_ANSWERS.mapNotNull { wib -> wib.get(request) }.size
  if (actualNonNullAnswers < MIN_MST_ANSWERS_SIZE) {
    MST_MINIMUM_ANSWERS.forEach { missingFields.addIfNull(request, it) }
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}
