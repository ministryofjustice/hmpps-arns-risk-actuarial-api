package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate

data class SNSVStaticRequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val assessmentDate: LocalDate,
  val dateOfCurrentConviction: LocalDate,
  val currentOffence: String,
  val totalNumberOfSanctions: Int,
  val ageAtFirstSanction: Int,
  val inCustodyOrCommunity: CustodyOrCommunity,
  val dateAtStartOfFollowup: LocalDate,
  val totalNumberOfViolentSanctions: Int,
)
