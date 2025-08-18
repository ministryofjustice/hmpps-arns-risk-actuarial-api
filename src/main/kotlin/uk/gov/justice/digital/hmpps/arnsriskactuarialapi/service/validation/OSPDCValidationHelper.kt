package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ospdcInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = ospdcValidationMissingFields(request) + sexualHistoryValidation(request)

internal fun sexualHistoryValidation(
  request: RiskScoreRequest,
): List<ValidationErrorResponse> = if (request.hasCommittedSexualOffence == false) {
  addMissingCriteriaValidation(arrayListOf(RiskScoreRequest::hasCommittedSexualOffence.name), emptyList())
} else {
  emptyList()
}

internal fun ospdcValidationMissingFields(
  request: RiskScoreRequest,
): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::hasCommittedSexualOffence)
  missingFields.addIfNull(request, RiskScoreRequest::totalContactAdultSexualSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::totalContactChildSexualSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::totalNonContactSexualOffences)
  missingFields.addIfNull(request, RiskScoreRequest::totalIndecentImageSanctions)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfMostRecentSexualOffence)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)

  return addMissingFields(missingFields, emptyList())
}
