package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import java.time.LocalDate

data class RiskScoreRequest(
  val version: String,
  val gender: Gender? = null,
  val dateOfBirth: LocalDate? = null,
  val dateOfCurrentConviction: LocalDate? = null,
  val dateAtStartOfFollowup: LocalDate? = null,
  val totalNumberOfSanctions: Integer? = null,
  val ageAtFirstSanction: Integer? = null,
  val currentOffence: String? = null,

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

  // OGP additional properties
  val regularOffendingActivities: ProblemLevel? = null,
  val currentDrugMisuse: ProblemLevel? = null,
  val motivationDrug: ProblemLevel? = null,
  val problemSolvingSkills: ProblemLevel? = null,
  val awarenessOfConsequences: ProblemLevel? = null,
  val understandsPeoplesViews: ProblemLevel? = null,

  // MST props
  val peerGroupInfluences: Boolean? = null,
  val attitudesPeerPressure: ProblemLevel? = null,
  val attitudesStableBehaviour: ProblemLevel? = null,
  val difficultiesCoping: ProblemLevel? = null,
  val attitudesTowardsSelf: ProblemLevel? = null,
  val impulsivityBehaviour: ProblemLevel? = null,

  // PNI
  val community: Boolean? = null,
  val hasCommittedSexualOffence: Boolean? = null,
  val riskSexualHarm: Boolean? = null,
  val sexualPreoccupation: ProblemLevel? = null,
  val sexualInterestsOffenceRelated: ProblemLevel? = null,
  val emotionalCongruence: ProblemLevel? = null,
  val hostileOrientation: ProblemLevel? = null,
  val currentRelationshipFamilyMembers: ProblemLevel? = null,
  val previousCloseRelationships: ProblemLevel? = null,
  val easilyInfluencedByCriminals: ProblemLevel? = null,
  val controllingBehaviour: ProblemLevel? = null,
  val saraRiskToPartner: RiskBand? = null,
  val saraRiskToOthers: RiskBand? = null,
)
