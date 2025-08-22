package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.collections.arrayListOf

class OSPIICValidationHelper {
  companion object {

    val OSPIIC_MALE_REQUIRED_PROPERTIES = listOf(
      RiskScoreRequest::totalContactAdultSexualSanctions.name,
      RiskScoreRequest::totalContactChildSexualSanctions.name,
      RiskScoreRequest::totalIndecentImageSanctions.name,
      RiskScoreRequest::totalNonContactSexualOffences.name,
    )

    fun ospiicInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = when (request.gender) {
      null -> addMissingFields(listOf(RiskScoreRequest::gender.name), emptyList())
      Gender.FEMALE -> emptyList()
      Gender.MALE -> ospiicMissingFieldsValidation(request) + ospiicSexualOffenceTrueValidation(request)
    }

    fun ospiicMissingFieldsValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = addMissingFields(
      getMissingPropertiesErrorStrings(request, OSPIIC_MALE_REQUIRED_PROPERTIES),
      emptyList(),
    )

    fun ospiicSexualOffenceTrueValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = if (request.hasEverCommittedSexualOffence == true &&
      request.totalContactAdultSexualSanctions == 0 &&
      request.totalContactChildSexualSanctions == 0 &&
      request.totalIndecentImageSanctions == 0 &&
      request.totalNonContactSexualOffences == 0
    ) {
      arrayListOf(
        ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: Sexual motivation/offending identified - please complete sexual offence counts.",
          fields = arrayListOf(
            RiskScoreRequest::totalContactAdultSexualSanctions.name,
            RiskScoreRequest::totalContactChildSexualSanctions.name,
            RiskScoreRequest::totalIndecentImageSanctions.name,
            RiskScoreRequest::totalNonContactSexualOffences.name,
          ),
        ),
      )
    } else {
      emptyList()
    }
  }
}
