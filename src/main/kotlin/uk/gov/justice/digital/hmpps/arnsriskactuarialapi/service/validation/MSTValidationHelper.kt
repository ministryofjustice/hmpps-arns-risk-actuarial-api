package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstAge
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isValidMstGender

fun mstInitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()

  val missingFields = mutableListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  if (request.peerGroupInfluences == null) missingFields.add("Peer group influences")

  return addMissingFields(missingFields, errors)
}

fun genderAndAgeValidation(
  gender: Gender,
  age: Int,
  errors: MutableList<ValidationErrorResponse>,
): MutableList<ValidationErrorResponse> {
  val criteriaFields = mutableListOf<String>()

  if (!isValidMstGender(gender)) criteriaFields.add("Gender")
  if (!isValidMstAge(age)) criteriaFields.add("Date of birth")

  return addMissingCriteriaValidation(criteriaFields, errors)
}
