package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.YesSometimesNo
import java.time.LocalDate

data class MSTRequestValidated(
  val gender: Gender,
  val assessmentDate: LocalDate,
  val dateOfBirth: LocalDate,
  val hasPeerGroupInfluences: Boolean?,
  val influenceFromCriminalAssociates: ProblemLevel?,
  val recklessnessAndRiskTakingBehaviour: ProblemLevel?,
  val difficultiesCoping: ProblemLevel?,
  val attitudesTowardsSelf: ProblemLevel?,
  val impulsivityProblems: ProblemLevel?,
  val temperControl: ProblemLevel?,
  val problemSolvingSkills: ProblemLevel?,
  val awarenessOfConsequences: YesSometimesNo?,
  val understandsOtherPeoplesViews: ProblemLevel?,
)
