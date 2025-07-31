package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

data class SNSVDynamicRequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val assessmentDate: LocalDate,
  val dateOfCurrentConviction: LocalDate,
  val currentOffence: String,
  val totalNumberOfSanctions: Int,
  val ageAtFirstSanction: Int,
  val custody: Boolean,
  val dateOfCommunitySentenceOrEarliestRelease: LocalDate,
  val totalNumberOfViolentSanctions: Int,
  val carryingOrUsingWeapon: Boolean?,
  val suitabilityOfAccommodation: ProblemLevel?,
  val employmentStatus: Boolean?,
  val currentRelationshipWithPartner: ProblemLevel?,
  val alcoholIsCurrentUseAProblem: ProblemLevel?,
  val alcoholExcessive6Months: ProblemLevel?,
  val impulsivityBehaviour: ProblemLevel?,
  val temperControl: ProblemLevel?,
  val proCriminalAttitudes: ProblemLevel?,
  val domesticAbuse: Boolean?,
  val previousConvictions: List<PreviousConviction>?,
)
