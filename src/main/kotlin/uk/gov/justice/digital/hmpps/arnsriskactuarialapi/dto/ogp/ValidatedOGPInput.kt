package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

data class ValidatedOGPInput(
  val algorithmVersion: String,
  val ogrs3TwoYear: Double,
  val currentAccomodation: Boolean,
  val employmentStatus: Boolean,
  val regularOffendingActivities: ProblemsGrading,
  val currentDrugMisuse: ProblemsGrading,
  val motivationDrug: ProblemsGrading,
  val problemSolvingSkills: ProblemsGrading,
  val awarenessOfConsequences: ProblemsGrading,
  val understandsPeoplesViews: ProblemsGrading,
  val proCriminalAttitudes: ProblemsGrading,
)
