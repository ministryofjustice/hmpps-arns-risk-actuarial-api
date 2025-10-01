package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

val OVP_REQUIRED_FIELDS = listOf(
  RiskScoreRequest::gender,
  RiskScoreRequest::dateOfBirth,
  RiskScoreRequest::dateAtStartOfFollowupCalculated,
  RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  RiskScoreRequest::totalNumberOfViolentSanctions,
  RiskScoreRequest::doesRecogniseImpactOfOffendingOnOthers,
  RiskScoreRequest::isCurrentlyOfNoFixedAbodeOrTransientAccommodation,
  RiskScoreRequest::isUnemployed,
  RiskScoreRequest::currentAlcoholUseProblems,
  RiskScoreRequest::excessiveAlcoholUse,
  RiskScoreRequest::hasCurrentPsychiatricTreatment,
  RiskScoreRequest::temperControl,
  RiskScoreRequest::proCriminalAttitudes,
)

fun validateOVP(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = mutableListOf<ValidationErrorResponse>()
  validateRequiredFields(request, errors, OVP_REQUIRED_FIELDS)
  validateTotalNumberOfSanctionsForAllOffences(request, errors)
  return errors
}
