package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.time.LocalDate

data class OSPDCRequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val assessmentDate: LocalDate,
  val hasCommittedSexualOffence: Boolean,
  val dateOfCurrentConviction: LocalDate,
  val totalContactAdultSexualSanctions: Int,
  val totalContactChildSexualSanctions: RiskBand,
  val totalNonContactSexualOffences: Gender,
  val totalIndecentImageSanctions: Integer,
  val dateAtStartOfFollowup: LocalDate,
  val dateOfMostRecentSexualOffence: LocalDate,
  val totalNumberOfSanctions: Int,
  val victimStranger: Boolean,
  val inCustodyOrCommunity: CustodyOrCommunity,
  val mostRecentOffenceDate: LocalDate,
)
