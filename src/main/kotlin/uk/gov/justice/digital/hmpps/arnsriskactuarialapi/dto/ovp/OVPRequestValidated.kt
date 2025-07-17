package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ovp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

data class OVPRequestValidated(
  val version: String,
  val totalNumberOfSanctions: Int,
  val totalNumberOfViolentSanctions: Integer,
  val dateAtStartOfFollowup: LocalDate,
  val dateOfBirth: LocalDate,
  val gender: Gender,
  val impactOfOffendingOnOthers: Boolean,
  val currentAccommodation: Boolean,
  val employmentStatus: Boolean,
  val alcoholIsCurrentUseAProblem: ProblemLevel,
  val alcoholExcessive6Months: ProblemLevel,
  val currentPsychiatricTreatmentOrPending: Boolean,
  val temperControl: ProblemLevel,
  val proCriminalAttitudes: ProblemLevel,
)
