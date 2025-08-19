package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

data class OVPRequestValidated(
  val totalNumberOfSanctionsForAllOffences: Int,
  val totalNumberOfViolentSanctions: Int,
  val dateAtStartOfFollowup: LocalDate,
  val dateOfBirth: LocalDate,
  val gender: Gender,
  val doesRecogniseImpactOfOffendingOnOthers: Boolean,
  val isCurrentlyOfNoFixedAbodeOrTransientAccommodation: Boolean,
  val isUnemployed: Boolean,
  val currentAlcoholUseProblems: ProblemLevel,
  val excessiveAlcoholUse: ProblemLevel,
  val hasCurrentPsychiatricTreatment: Boolean,
  val temperControl: ProblemLevel,
  val proCriminalAttitudes: ProblemLevel,
)
