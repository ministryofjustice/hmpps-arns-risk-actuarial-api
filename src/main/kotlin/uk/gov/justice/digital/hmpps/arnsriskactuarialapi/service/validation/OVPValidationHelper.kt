package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

fun validateOVP(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfViolentSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::doesRecogniseImpactOfOffendingOnOthers)
  missingFields.addIfNull(request, RiskScoreRequest::isCurrentlyOfNoFixedAbodeOrTransientAccommodation)
  missingFields.addIfNull(request, RiskScoreRequest::isUnemployed)
  missingFields.addIfNull(request, RiskScoreRequest::currentAlcoholUseProblems)
  missingFields.addIfNull(request, RiskScoreRequest::excessiveAlcoholUse)
  missingFields.addIfNull(request, RiskScoreRequest::hasCurrentPsychiatricTreatment)
  missingFields.addIfNull(request, RiskScoreRequest::temperControl)
  missingFields.addIfNull(request, RiskScoreRequest::proCriminalAttitudes)

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_INPUT.asErrorResponse(missingFields)
  }
}
