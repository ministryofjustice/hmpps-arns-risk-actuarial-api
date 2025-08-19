package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo

data class OGPInputValidated(
  val ogrs3TwoYear: Int,
  val isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean,
  val isUnemployed: Boolean,
  val regularOffendingActivities: ProblemLevel,
  val currentDrugMisuse: ProblemLevel,
  val motivationToTackleDrugMisuse: MotivationLevel,
  val problemSolvingSkills: ProblemLevel,
  val awarenessOfConsequences: YesSometimesNo,
  val understandsPeoplesViews: ProblemLevel,
  val proCriminalAttitudes: ProblemLevel,
)
