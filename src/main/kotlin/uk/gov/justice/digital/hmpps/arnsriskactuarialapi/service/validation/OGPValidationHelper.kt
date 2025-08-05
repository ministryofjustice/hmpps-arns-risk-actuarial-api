package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object

class OGPValidationHelper {
  companion object {

    val OGP_PROPERTIES = listOf(
      RiskScoreRequest::currentAccommodation.name,
      RiskScoreRequest::employmentStatus.name,
      RiskScoreRequest::regularOffendingActivities.name,
      RiskScoreRequest::currentDrugMisuse.name,
      RiskScoreRequest::motivationDrug.name,
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
  }
}
