package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ospdcInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = when (request.gender) {
  null -> addMissingFields(listOf(RiskScoreRequest::gender.name), emptyList())
  Gender.MALE -> ospdcValidationForMale(request) + sexualHistoryValidation(request)
  else -> ospdcValidationForFemale(request)
}

internal fun sexualHistoryValidation(
  request: RiskScoreRequest,
): List<ValidationErrorResponse> = if (request.hasCommittedSexualOffence == false) {
  addMissingCriteriaValidation(arrayListOf(RiskScoreRequest::hasCommittedSexualOffence.name), emptyList())
} else {
  emptyList()
}

internal fun ospdcValidationForMale(
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
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctions)

  return addMissingFields(missingFields, emptyList())
}

internal fun ospdcValidationForFemale(
  request: RiskScoreRequest,
): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()
  missingFields.addIfNull(request, RiskScoreRequest::hasCommittedSexualOffence)
  return addMissingFields(missingFields, emptyList())
}
