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

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  if (request.dateAtStartOfFollowup == null) missingFields.add("Date at start of followup")
  if (request.totalNumberOfSanctions == null) missingFields.add("Total number of sanctions")
  if (request.totalNumberOfViolentSanctions == null) missingFields.add("Total number of violent sanctions")
  if (request.impactOfOffendingOnOthers == null) missingFields.add("Impact of offending on others")
  if (request.currentAccommodation == null) missingFields.add("Current accommodation")
  if (request.employmentStatus == null) missingFields.add("Employment status")
  if (request.alcoholIsCurrentUseAProblem == null) missingFields.add("Alcohol is current use a problem")
  if (request.alcoholExcessive6Months == null) missingFields.add("Alcohol excessive 6 months")
  if (request.currentPsychiatricTreatmentOrPending == null) missingFields.add("Current psychiatric treatment or pending")
  if (request.temperControl == null) missingFields.add("Temper control")
  if (request.proCriminalAttitudes == null) missingFields.add("Pro criminal attitudes")

  return addMissingFields(missingFields, errors)
}
