package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import java.time.LocalDate

data class SNSVDynamicRequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val assessmentDate: LocalDate,
  val dateOfCurrentConviction: LocalDate,
  val currentOffenceCode: String,
  val totalNumberOfSanctionsForAllOffences: Int,
  val ageAtFirstSanction: Int,
  val supervisionStatus: SupervisionStatus,
  val dateAtStartOfFollowup: LocalDate,
  val totalNumberOfViolentSanctions: Int,
  val didOffenceInvolveCarryingOrUsingWeapon: Boolean,
  val suitabilityOfAccommodation: ProblemLevel,
  val isUnemployed: Boolean,
  val currentRelationshipWithPartner: ProblemLevel,
  val currentAlcoholUseProblems: ProblemLevel,
  val excessiveAlcoholUse: ProblemLevel,
  val impulsivityProblems: ProblemLevel,
  val temperControl: ProblemLevel,
  val proCriminalAttitudes: ProblemLevel,
  val domesticViolencePerpetrator: Boolean,
  val previousConvictions: List<PreviousConviction>,
)
