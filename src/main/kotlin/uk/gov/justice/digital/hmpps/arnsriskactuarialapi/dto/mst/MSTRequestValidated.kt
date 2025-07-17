package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

data class MSTRequestValidated(
  val version: String,
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val peerGroupInfluences: Boolean,
  val attitudesPeerPressure: LocalDate,
  val attitudesStableBehaviour: ProblemLevel,
  val difficultiesCoping: ProblemLevel,
  val attitudesTowardsSelf: ProblemLevel,
  val impusilvityBehaviour: ProblemLevel,
  val temperControl: ProblemLevel,
  val problemSolvingSkills: ProblemLevel,
  val awarenessOfConsequences: ProblemLevel,
  val understandsPeoplesViews: ProblemLevel,
)
