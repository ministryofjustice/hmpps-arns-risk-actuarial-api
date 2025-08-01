package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

data class OPDRequestValidated(
  val overallRiskForAssessment: RiskBand,
  val highestRiskLevel: RiskBand,
  val gender: Gender,
  val ageAtFirstSanction: Int? = null,
  val currentOffence: String,
  val opdOverride: Boolean,
  val eligibleForMappa: Boolean,
  val carryingOrUsingWeapon: Boolean,
  val violenceOrThreatOfViolence: Boolean,
  val excessiveOrSadisticViolence: Boolean,
  val offenceArson: Boolean,
  val offenderMotivations: Boolean,
  val offenceLinkedRiskOfSeriousHarm: Boolean,
  val accommodationLinkedRiskOfSeriousHarm: Boolean,
  val experienceOfChildhood: ProblemLevel? = null,
  val difficultiesCoping: ProblemLevel? = null,
  val domesticAbuse: Boolean,
  val domesticAbusePartner: Boolean?,
  val domesticAbuseFamily: Boolean?,
  val relationshipLinkedSeriousHarm: Boolean,
  val currentPsychologicalProblems: ProblemLevel? = null,
  val currentPsychiatricTreatmentOrPending: Boolean?,
  val wellbeingEmotionalLinkedRiskOfSeriousHarm: Boolean,
  val thinkingAndBehaviourLinedToRiskOfSeriousHarm: Boolean,
  val custodialSentence: Boolean,
  val financialRelianceOnOthers: ProblemLevel? = null,
  val manipulativePredatoryBehaviour: ProblemLevel? = null,
  val childhoodBehaviour: ProblemLevel? = null,
  val currentPsychiatricProblems: ProblemLevel? = null,
  val historyOfPsychiatricTreatment: Boolean,
  val medicationMentalHealth: Boolean,
  val patientSecureUnitOrHospital: Boolean,
  val obsessiveBehaviour: Boolean,
  val selfHarmSuicideAttempt: Boolean?,
  val concernsAboutSuicidePast: Boolean?,
  val concernsAboutSelfHarmPast: Boolean?,
  val attitudeTowardsSupervision: ProblemLevel? = null,
  val controllingBehaviour: ProblemLevel? = null,
  val assaultedOrThreatenedStaff: Boolean?,
  val escapeOrAbsconded: Boolean?,
  val controlIssues: Boolean?,
  val breachOfTrust: Boolean?,
  val impactOfOffendingOnOthers: Boolean,
  val attitudesStableBehaviour: ProblemLevel? = null,
  val impulsivityBehaviour: ProblemLevel? = null,
)
