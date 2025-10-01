package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

val OSP_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::gender,
)

val OSP_CONDITIONAL_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::totalContactAdultSexualSanctions,
  RiskScoreRequest::totalContactChildSexualSanctions,
  RiskScoreRequest::totalNonContactSexualOffences,
  RiskScoreRequest::totalIndecentImageSanctions,
)

val OSPDC_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
  RiskScoreRequest::hasEverCommittedSexualOffence,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateAtStartOfFollowup,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::supervisionStatus,
)

fun validateOSP(request: RiskScoreRequest, isOSPDC: Boolean): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors, isOSPDC)
  return errors
}

private fun validateRequiredFields(request: RiskScoreRequest, errors: MutableList<ValidationErrorResponse>, isOSPDC: Boolean) {
  val missingFields = arrayListOf<String>()
  val sexualOffendingInconsistentFields = arrayListOf<String>()
  val missingSexualSanctionCounts = arrayListOf<String>()

  OSP_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }
  if (isOSPDC) OSPDC_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }

  // Sexual sanction validation
  if (request.gender == Gender.MALE) {
    when (request.hasEverCommittedSexualOffence) {
      false -> {
        OSP_CONDITIONAL_FIELDS.forEach { sexualOffendingInconsistentFields.addIfNotNull(request, it) }
      }
      true -> {
        missingFields.addIfNull(request, RiskScoreRequest::isCurrentOffenceSexuallyMotivated)

        OSP_CONDITIONAL_FIELDS.forEach { missingSexualSanctionCounts.addIfNull(request, it) }

        val noSanctions = listOf(
          request.totalContactAdultSexualSanctions,
          request.totalContactChildSexualSanctions,
          request.totalIndecentImageSanctions,
          request.totalNonContactSexualOffences,
        ).all { it == 0 }

        if (noSanctions) {
          missingSexualSanctionCounts.add(RiskScoreRequest::totalContactAdultSexualSanctions.name)
          missingSexualSanctionCounts.add(RiskScoreRequest::totalContactChildSexualSanctions.name)
          missingSexualSanctionCounts.add(RiskScoreRequest::totalIndecentImageSanctions.name)
          missingSexualSanctionCounts.add(RiskScoreRequest::totalNonContactSexualOffences.name)
        }
      }
      null -> if (!isOSPDC) missingFields.addIfNull(request, RiskScoreRequest::hasEverCommittedSexualOffence)
    }
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }

  if (sexualOffendingInconsistentFields.isNotEmpty()) {
    errors += ValidationErrorType.SEXUAL_OFFENDING_INCONSISTENT_INPUT.asErrorResponse(sexualOffendingInconsistentFields)
  }

  if (missingSexualSanctionCounts.isNotEmpty()) {
    errors += ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS.asErrorResponse(missingSexualSanctionCounts)
  }
}
