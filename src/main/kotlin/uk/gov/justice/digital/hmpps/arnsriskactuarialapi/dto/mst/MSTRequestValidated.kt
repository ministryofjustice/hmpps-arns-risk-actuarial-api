package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

data class MSTRequestValidated(
  val gender: Gender,
  val assessmentDate: LocalDate,
  val dateOfBirth: LocalDate,
  val peerGroupInfluences: Boolean?,
  val attitudesPeerPressure: ProblemLevel?,
  val attitudesStableBehaviour: ProblemLevel?,
  val difficultiesCoping: ProblemLevel?,
  val attitudesTowardsSelf: ProblemLevel?,
  val impulsivityBehaviour: ProblemLevel?,
  val temperControl: ProblemLevel?,
  val problemSolvingSkills: ProblemLevel?,
  val awarenessOfConsequences: ProblemLevel?,
  val understandsPeoplesViews: ProblemLevel?,
)
