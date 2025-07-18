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
  val totalNumberOfViolentSanctions: Integer? = null,
  val impactOfOffendingOnOthers: Boolean? = null,
  val currentAccommodation: Boolean? = null,
  val employmentStatus: Boolean? = null,
  val alcoholIsCurrentUseAProblem: ProblemLevel? = null,
  val alcoholExcessive6Months: ProblemLevel? = null,
  val currentPsychiatricTreatmentOrPending: Boolean? = null,
  val temperControl: ProblemLevel? = null,
  val proCriminalAttitudes: ProblemLevel? = null,

  // MST props
  val peerGroupInfluences: Boolean? = null,
  val attitudesPeerPressure: LocalDate? = null,
  val attitudesStableBehaviour: ProblemLevel? = null,
  val difficultiesCoping: ProblemLevel? = null,
  val attitudesTowardsSelf: ProblemLevel? = null,
  val impusilvityBehaviour: ProblemLevel? = null,
  val problemSolvingSkills: ProblemLevel? = null,
  val awarenessOfConsequences: ProblemLevel? = null,
  val understandsPeoplesViews: ProblemLevel? = null,
)
