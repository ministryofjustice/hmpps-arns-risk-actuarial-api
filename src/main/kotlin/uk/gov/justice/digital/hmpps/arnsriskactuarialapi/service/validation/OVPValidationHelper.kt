package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ovpInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingOVPFieldsValidation(request)
  val totalSanctionsValidationErrorStep =
    getTotalNumberOfSanctionsForAllOffencesValidation(request.totalNumberOfSanctionsForAllOffences, missingFieldValidationErrorStep)
  return totalSanctionsValidationErrorStep
}

fun getMissingOVPFieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfViolentSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::doesRecogniseImpactOfOffendingOnOthers)
  missingFields.addIfNull(request, RiskScoreRequest::isCurrentlyOfNoFixedAbodeOrTransientAccommodation)
  missingFields.addIfNull(request, RiskScoreRequest::employmentStatus)
  missingFields.addIfNull(request, RiskScoreRequest::alcoholIsCurrentUseAProblem)
  missingFields.addIfNull(request, RiskScoreRequest::alcoholExcessive6Months)
  missingFields.addIfNull(request, RiskScoreRequest::currentPsychiatricTreatmentOrPending)
  missingFields.addIfNull(request, RiskScoreRequest::temperControl)
  missingFields.addIfNull(request, RiskScoreRequest::proCriminalAttitudes)

  return addMissingFields(missingFields, errors)
}
