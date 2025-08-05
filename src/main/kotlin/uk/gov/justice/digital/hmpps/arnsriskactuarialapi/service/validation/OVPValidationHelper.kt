package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ovpInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingOVPFieldsValidation(request)
  val totalSanctionsValidationErrorStep =
    getTotalNumberOfSanctionsValidation(request.totalNumberOfSanctions, missingFieldValidationErrorStep)
  return totalSanctionsValidationErrorStep
}

fun getMissingOVPFieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val missingFields = arrayListOf<String>()

  if (request.gender == null) missingFields.add("gender")
  if (request.dateOfBirth == null) missingFields.add("dateOfBirth")
  if (request.dateAtStartOfFollowup == null) missingFields.add("dateAtStartOfFollowup")
  if (request.totalNumberOfSanctions == null) missingFields.add("totalNumberOfSanctions")
  if (request.totalNumberOfViolentSanctions == null) missingFields.add("totalNumberOfViolentSanctions")
  if (request.impactOfOffendingOnOthers == null) missingFields.add("impactOfOffendingOnOthers")
  if (request.currentAccommodation == null) missingFields.add("currentAccommodation")
  if (request.employmentStatus == null) missingFields.add("employmentStatus")
  if (request.alcoholIsCurrentUseAProblem == null) missingFields.add("alcoholIsCurrentUseAProblem")
  if (request.alcoholExcessive6Months == null) missingFields.add("alcoholExcessive6Months")
  if (request.currentPsychiatricTreatmentOrPending == null) missingFields.add("currentPsychiatricTreatmentOrPending")
  if (request.temperControl == null) missingFields.add("temperControl")
  if (request.proCriminalAttitudes == null) missingFields.add("proCriminalAttitudes")

  return addMissingFields(missingFields, errors)
}
