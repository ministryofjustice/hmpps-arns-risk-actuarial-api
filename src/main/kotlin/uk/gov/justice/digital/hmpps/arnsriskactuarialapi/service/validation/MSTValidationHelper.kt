package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstGender

const val MIN_MST_ANSWERS_SIZE = 9

fun mstInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)

  val answersCount = listOfNotNull(
    request.hasPeerGroupInfluences,
    request.influenceFromCriminalAssociates,
    request.recklessnessAndRiskTakingBehaviour,
    request.difficultiesCoping,
    request.attitudesTowardsSelf,
    request.impulsivityProblems,
    request.temperControl,
    request.problemSolvingSkills,
    request.awarenessOfConsequences,
    request.understandsOtherPeoplesViews,
  ).size

  if (answersCount < MIN_MST_ANSWERS_SIZE) {
    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    missingFields.addIfNull(request, RiskScoreRequest::influenceFromCriminalAssociates)
    missingFields.addIfNull(request, RiskScoreRequest::recklessnessAndRiskTakingBehaviour)
    missingFields.addIfNull(request, RiskScoreRequest::difficultiesCoping)
    missingFields.addIfNull(request, RiskScoreRequest::attitudesTowardsSelf)
    missingFields.addIfNull(request, RiskScoreRequest::impulsivityProblems)
    missingFields.addIfNull(request, RiskScoreRequest::temperControl)
    missingFields.addIfNull(request, RiskScoreRequest::problemSolvingSkills)
    missingFields.addIfNull(request, RiskScoreRequest::awarenessOfConsequences)
    missingFields.addIfNull(request, RiskScoreRequest::understandsOtherPeoplesViews)
  }
  return addMissingFields(missingFields, errors)
}

fun genderAndAgeValidation(
  gender: Gender,
  age: Int,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val criteriaFields = arrayListOf<String>()

  if (!isValidMstGender(gender)) criteriaFields.add(RiskScoreRequest::gender.name)
  if (!isValidMstAge(age)) criteriaFields.add(RiskScoreRequest::dateOfBirth.name)

  return addMissingCriteriaValidation(criteriaFields, errors)
}
