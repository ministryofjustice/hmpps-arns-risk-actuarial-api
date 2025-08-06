package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

fun ospdcInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> {
  val errors = arrayListOf<ValidationErrorResponse>()

  val genderWithSexualHistoryValidationErrors = getGenderWithSexualHistoryValidation(request, errors)
  val missingOSPDCFieldsValidationErrors = getMissingOSPDCFieldsValidation(request, genderWithSexualHistoryValidationErrors)

  return missingOSPDCFieldsValidationErrors
}

fun getGenderWithSexualHistoryValidation(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): List<ValidationErrorResponse> {
  val criteriaFields = arrayListOf<String>()

  if (request.hasCommittedSexualOffence == false) criteriaFields.add("hasCommittedSexualOffence")
  if (request.hasCommittedSexualOffence == true && request.gender == Gender.FEMALE) criteriaFields.add("Gender")

  return addMissingCriteriaValidation(criteriaFields, errors)
}

fun getMissingOSPDCFieldsValidation(request: RiskScoreRequest, errors: List<ValidationErrorResponse>): List<ValidationErrorResponse> {
  val missingFields = arrayListOf<String>()

  if (request.gender == null) missingFields.add("Gender")
  if (request.dateOfBirth == null) missingFields.add("Date of birth")
  if (request.hasCommittedSexualOffence == null) missingFields.add("Has commited sexual offence")
  if (request.totalContactAdultSexualSanctions == null) missingFields.add("Total contact adult sexual sanctions")
  if (request.totalContactChildSexualSanctions == null) missingFields.add("Total contact child sexual sanctions")
  if (request.totalNonContactSexualOffences == null) missingFields.add("Total non contact sexual offences")
  if (request.totalIndecentImageSanctions == null) missingFields.add("Total indecent image sanctions")
  if (request.dateAtStartOfFollowup == null) missingFields.add("Date at start of followup")
  if (request.dateOfMostRecentSexualOffence == null) missingFields.add("Date of most recent sexual offence")
  if (request.totalNumberOfSanctions == null) missingFields.add("Total number of sanctions")

  return addMissingFields(missingFields, errors)
}
