package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object

val OGP_REQUIRED_FIELDS_FROM_REQUEST = listOf(
  RiskScoreRequest::isCurrentlyOfNoFixedAbodeOrTransientAccommodation,
  RiskScoreRequest::isUnemployed,
  RiskScoreRequest::regularOffendingActivities,
  RiskScoreRequest::currentDrugMisuse,
  RiskScoreRequest::motivationToTackleDrugMisuse,
  RiskScoreRequest::problemSolvingSkills,
  RiskScoreRequest::awarenessOfConsequences,
  RiskScoreRequest::understandsOtherPeoplesViews,
  RiskScoreRequest::proCriminalAttitudes,
)

private fun validateRequiredFields(request: RiskScoreRequest, context: RiskScoreContext, errors: MutableList<ValidationErrorResponse>) {
  val missingFields = arrayListOf<String>()

  OGP_REQUIRED_FIELDS_FROM_REQUEST.forEach { field -> missingFields.addIfNull(request, field) }

  if (context.OGRS3?.ogrs3TwoYear == null) {
    missingFields.add(OGRS3Object::ogrs3TwoYear.name)
  }

  if (missingFields.isNotEmpty()) {
    errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asErrorResponse(missingFields)
  }
}

fun validateOGP(request: RiskScoreRequest, context: RiskScoreContext): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, context, errors)
  return errors
}
