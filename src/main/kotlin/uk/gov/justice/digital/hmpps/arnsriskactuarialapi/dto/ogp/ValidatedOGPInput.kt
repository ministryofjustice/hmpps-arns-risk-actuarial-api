package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel

data class ValidatedOGPInput(
  val algorithmVersion: String,
  val ogrs3TwoYear: Int,
  val currentAccomodation: Boolean,
  val employmentStatus: Boolean,
  val regularOffendingActivities: ProblemLevel,
  val currentDrugMisuse: ProblemLevel,
  val motivationDrug: ProblemLevel,
  val problemSolvingSkills: ProblemLevel,
  val awarenessOfConsequences: ProblemLevel,
  val understandsPeoplesViews: ProblemLevel,
  val proCriminalAttitudes: ProblemLevel,
)
