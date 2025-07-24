package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext

class OGPValidationHelper {
  companion object {

    val PROPERTIES_TO_ERRORS = mapOf(
      "currentAccommodation" to "Current accommodation",
      "employmentStatus" to "Employment status",
      "regularOffendingActivities" to "Regular offending activities",
      "currentDrugMisuse" to "Current drug misuse",
      "motivationDrug" to "Motivation drug",
      "problemSolvingSkills" to "Problem solving skills",
      "awarenessOfConsequences" to "Awareness of consequences",
      "understandsPeoplesViews" to "Understands Peoples Views",
      "proCriminalAttitudes" to "Procriminal attitudes",
    )

    fun getMissingFieldsErrorsInContext(context: RiskScoreContext): List<String> = if (context.OGRS3 == null || context.OGRS3.ogrs3TwoYear == null) {
      listOf("OGRS3 Two Year")
    } else {
      emptyList()
    }
  }
}
