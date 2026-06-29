package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import java.time.LocalDate

data class DirectContactSexualReoffendingPredictorRequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val hasEverCommittedSexualOffence: Boolean,
  val totalContactAdultSexualSanctions: Int,
  val totalContactChildSexualSanctions: Int,
  val totalNonContactSexualOffences: Int,
  val totalIndecentImageSanctions: Int,
  val dateAtStartOfFollowup: LocalDate,
  val totalNumberOfSanctionsForAllOffences: Int,
  val dateOfMostRecentSexualOffence: LocalDate?,
  val isCurrentOffenceAgainstVictimStranger: Boolean?,
  val supervisionStatus: SupervisionStatus,
  val mostRecentOffenceDate: LocalDate?,
  val assessmentDate: LocalDate,
)
