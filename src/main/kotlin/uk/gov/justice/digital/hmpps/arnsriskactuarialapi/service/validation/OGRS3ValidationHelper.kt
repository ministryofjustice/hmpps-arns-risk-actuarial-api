package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ogrs3InitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val missingFieldValidationErrorStep = getMissingOGRS3FieldsValidation(request)
  val totalSanctionsValidationErrorStep =
    getTotalNumberOfSanctionsForAllOffencesValidation(request.totalNumberOfSanctionsForAllOffences, emptyList())
  val currentOffenceCodeValidationErrorStep =
    getCurrentOffenceCodeValidation(request.currentOffenceCode, emptyList())
  return missingFieldValidationErrorStep + totalSanctionsValidationErrorStep + currentOffenceCodeValidationErrorStep
}

fun getMissingOGRS3FieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val missingFields = arrayListOf<String>()

  missingFields.addIfNull(request, RiskScoreRequest::gender)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfBirth)
  missingFields.addIfNull(request, RiskScoreRequest::dateOfCurrentConviction)
  missingFields.addIfNull(request, RiskScoreRequest::dateAtStartOfFollowup)
  missingFields.addIfNull(request, RiskScoreRequest::totalNumberOfSanctionsForAllOffences)
  missingFields.addIfNull(request, RiskScoreRequest::ageAtFirstSanction)
  missingFields.addIfNull(request, RiskScoreRequest::currentOffenceCode)

  return addMissingFields(missingFields, errors)
}
