package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate

data class OGRS3RequestValidated(
  val gender: Gender,
  val dateOfBirth: LocalDate,
  val dateOfCurrentConviction: LocalDate,
  val dateAtStartOfFollowup: LocalDate,
  val totalNumberOfSanctions: Int,
  val ageAtFirstSanction: Int,
  val currentOffence: String,
)
