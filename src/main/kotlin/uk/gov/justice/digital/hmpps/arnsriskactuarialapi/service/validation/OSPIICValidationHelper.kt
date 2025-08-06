package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

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
      Gender.MALE -> addMissingFields(
        getMissingPropertiesErrorStrings(request, OSPIIC_MALE_REQUIRED_PROPERTIES),
        emptyList(),
      )
    }
  }
}
