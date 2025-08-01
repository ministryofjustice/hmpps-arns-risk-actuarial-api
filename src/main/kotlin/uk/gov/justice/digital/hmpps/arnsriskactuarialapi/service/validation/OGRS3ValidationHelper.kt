package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ogrs3InitialValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingOGRS3FieldsValidation(request)
  val totalSanctionsValidationErrorStep =
    getTotalNumberOfSanctionsValidation(request.totalNumberOfSanctions, missingFieldValidationErrorStep)
  val currentOffenceValidationErrorStep =
    getCurrentOffenceValidation(request.currentOffence, totalSanctionsValidationErrorStep)
  return currentOffenceValidationErrorStep
}

fun getMissingOGRS3FieldsValidation(request: RiskScoreRequest): MutableList<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()

  val missingFields = mutableListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  if (request.dateOfCurrentConviction == null) missingFields.add("Date of current conviction")
  if (request.dateAtStartOfFollowup == null) missingFields.add("Date at start of followup")
  if (request.totalNumberOfSanctions == null) missingFields.add("Total number of sanctions")
  if (request.ageAtFirstSanction == null) missingFields.add("Age at first sanction")
  if (request.currentOffence == null) missingFields.add("Current offence")

  return addMissingFields(missingFields, errors)
}
