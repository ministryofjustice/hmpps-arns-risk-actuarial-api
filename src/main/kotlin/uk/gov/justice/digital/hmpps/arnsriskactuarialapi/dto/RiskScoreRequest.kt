package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import java.time.LocalDate

data class RiskScoreRequest(
  val version: String,
  val gender: Gender?,
  val dateOfBirth: LocalDate?,
  val dateOfCurrentConviction: LocalDate?,
  val dateAtStartOfFollowup: LocalDate?,
  val totalNumberOfSanctions: Integer?,
  val ageAtFirstSanction: Integer?,
  val currentOffence: String?,

  // OVP additional properties
  val impactOfOffendingOnOthers: Boolean?,
  val currentAccommodation: Boolean?,
  val employmentStatus: Boolean?,
  val alcoholIsCurrentUseAProblem: ProblemLevel?,
  val alcoholExcessive6Months: ProblemLevel?,
  val currentPsychiatricTreatmentOrPending: Boolean?,
  val temperControl: ProblemLevel?,
  val proCriminalAttitudes: ProblemLevel?,

  )

data class OGRS3RequestValidated(
  val version: String,
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val dateOfCurrentConviction: LocalDate,
  val dateAtStartOfFollowup: LocalDate,
  val totalNumberOfSanctions: Int,
  val ageAtFirstSanction: Int,
  val currentOffence: String,
)
