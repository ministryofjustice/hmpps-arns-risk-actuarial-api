package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse

class OSPIICValidationHelper {
  companion object {

    val OSPIIC_SEXUAL_OFFENCES = listOf(
      RiskScoreRequest::totalContactAdultSexualSanctions,
      RiskScoreRequest::totalContactChildSexualSanctions,
      RiskScoreRequest::totalIndecentImageSanctions,
      RiskScoreRequest::totalNonContactSexualOffences,
    )

    val ALL_OSPIIC_SEXUAL_OFFENCES_NAMES = OSPIIC_SEXUAL_OFFENCES.map { m -> m.name }.asSequence()

    fun ospiicInitialValidation(request: RiskScoreRequest): List<ValidationErrorResponse> = when (request.gender) {
      null -> addMissingFields(listOf(RiskScoreRequest::gender.name), emptyList())
      Gender.FEMALE -> emptyList()
      Gender.MALE -> if (request.hasEverCommittedSexualOffence == null) {
        (addMissingFields(listOf(RiskScoreRequest::hasEverCommittedSexualOffence.name), emptyList()))
      } else {
        offencesValidation(
          request.hasEverCommittedSexualOffence,
          getNullValuesFromProperties(request, OSPIIC_SEXUAL_OFFENCES),
        )
      }
    }

    private fun offencesValidation(
      hasEverCommittedSexualOffence: Boolean,
      nullValues: List<String>,
    ): List<ValidationErrorResponse> = when (hasEverCommittedSexualOffence) {
      false -> if (nullValues.size == 4) {
        emptyList()
      } else {
        addUnexpectedFields(
          ALL_OSPIIC_SEXUAL_OFFENCES_NAMES
            .minus(nullValues.toSet()).toList(),
          emptyList(),
        )
      }
      true -> TODO()
    }
  }
}
