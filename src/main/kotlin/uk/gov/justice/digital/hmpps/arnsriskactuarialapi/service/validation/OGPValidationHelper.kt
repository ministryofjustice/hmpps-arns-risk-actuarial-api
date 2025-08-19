package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object

class OGPValidationHelper {
  companion object {

    val OGP_PROPERTIES = listOf(
      RiskScoreRequest::isCurrentlyOfNoFixedAbodeOrTransientAccommodation.name,
      RiskScoreRequest::isUnemployed.name,
      RiskScoreRequest::regularOffendingActivities.name,
      RiskScoreRequest::currentDrugMisuse.name,
      RiskScoreRequest::motivationToTackleDrugMisuse.name,
      RiskScoreRequest::problemSolvingSkills.name,
      RiskScoreRequest::awarenessOfConsequences.name,
      RiskScoreRequest::understandsPeoplesViews.name,
      RiskScoreRequest::proCriminalAttitudes.name,
    )

    fun getMissingFieldsErrorsInContext(context: RiskScoreContext): List<String> = if (context.OGRS3 == null || context.OGRS3!!.ogrs3TwoYear == null) {
      listOf(OGRS3Object::ogrs3TwoYear.name)
    } else {
      emptyList()
    }

    fun ogpInitialValidation(request: RiskScoreRequest, context: RiskScoreContext): List<ValidationErrorResponse> {
      val missingProperties = getMissingPropertiesErrorStrings(request, OGP_PROPERTIES)
      val missingFields = getMissingFieldsErrorsInContext(context)
      return addMissingFields(missingProperties + missingFields, listOf())
    }
  }
}
