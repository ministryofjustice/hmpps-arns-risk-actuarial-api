package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

val OSPDC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::totalContactAdultSexualSanctions,
  RiskScoreRequest::totalContactChildSexualSanctions,
  RiskScoreRequest::totalNonContactSexualOffences,
  RiskScoreRequest::totalIndecentImageSanctions,
  RiskScoreRequest::dateAtStartOfFollowup,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::supervisionStatus,
)

fun validateOSPDC(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  OSPDC_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }

  if (request.gender == Gender.MALE &&
    request.hasEverCommittedSexualOffence == true &&
    request.isCurrentOffenceSexuallyMotivated == null
  ) {
    missingFields.addIfNull(request, RiskScoreRequest::isCurrentOffenceSexuallyMotivated)
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}
