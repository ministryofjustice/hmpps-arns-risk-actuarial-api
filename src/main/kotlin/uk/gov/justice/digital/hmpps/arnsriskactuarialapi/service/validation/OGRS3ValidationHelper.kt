package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ogrs3InitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingOGRS3FieldsValidation(request)
  val totalSanctionsValidationErrorStep =
    getTotalNumberOfSanctionsValidation(request.totalNumberOfSanctions, emptyList())
  val currentOffenceValidationErrorStep =
    getCurrentOffenceValidation(request.currentOffence, emptyList())
  return missingFieldValidationErrorStep + totalSanctionsValidationErrorStep + currentOffenceValidationErrorStep
}

fun getMissingOGRS3FieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val missingFields = arrayListOf<String>()

  if (request.gender == null) missingFields.add("gender")
  if (request.dateOfBirth == null) missingFields.add("dateOfBirth")
  if (request.dateOfCurrentConviction == null) missingFields.add("dateOfCurrentConviction")
  if (request.dateAtStartOfFollowup == null) missingFields.add("dateAtStartOfFollowup")
  if (request.totalNumberOfSanctions == null) missingFields.add("totalNumberOfSanctions")
  if (request.ageAtFirstSanction == null) missingFields.add("ageAtFirstSanction")
  if (request.currentOffence == null) missingFields.add("currentOffence")

  return addMissingFields(missingFields, errors)
}
