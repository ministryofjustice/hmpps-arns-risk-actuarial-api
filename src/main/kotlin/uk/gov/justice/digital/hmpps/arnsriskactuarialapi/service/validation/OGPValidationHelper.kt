package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext

class OGPValidationHelper {
  companion object {

    val OGP_PROPERTIES = listOf(
      "currentAccommodation",
      "employmentStatus",
      "regularOffendingActivities",
      "currentDrugMisuse",
      "motivationDrug",
      "problemSolvingSkills",
      "awarenessOfConsequences",
      "understandsPeoplesViews",
      "proCriminalAttitudes",
    )

    fun getMissingFieldsErrorsInContext(context: RiskScoreContext): List<String> =
      if (context.OGRS3 == null || context.OGRS3!!.ogrs3TwoYear == null) {
        listOf("ogrs3TwoYear")
      } else {
        emptyList()
      }
  }
}
