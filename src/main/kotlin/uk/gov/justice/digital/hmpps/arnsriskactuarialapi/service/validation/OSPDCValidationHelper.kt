package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ospdcInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val genderWithSexualHistoryValidationErrors = getGenderWithSexualHistoryValidation(request, errors)
  val missingOSPDCFieldsValidationErrors =
    getMissingOSPDCFieldsValidation(request, genderWithSexualHistoryValidationErrors)

  return missingOSPDCFieldsValidationErrors
}

fun getGenderWithSexualHistoryValidation(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
): List<ValidationErrorResponse> {
  val criteriaFields = arrayListOf<String>()

  if (request.hasCommittedSexualOffence == false) criteriaFields.add(RiskScoreRequest::hasCommittedSexualOffence.name)
  if (request.hasCommittedSexualOffence == true && request.gender == Gender.FEMALE) {
    criteriaFields.add(
      RiskScoreRequest::gender.name,
    )
  }

  return addMissingCriteriaValidation(criteriaFields, errors)
}

fun getMissingOSPDCFieldsValidation(
  request: RiskScoreRequest,
  errors: List<ValidationErrorResponse>,
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

  return addMissingFields(missingFields, errors)
}
