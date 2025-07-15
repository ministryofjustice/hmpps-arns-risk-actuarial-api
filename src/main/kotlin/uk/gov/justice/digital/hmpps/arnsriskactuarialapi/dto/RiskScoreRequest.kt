package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import java.time.LocalDate

data class RiskScoreRequest(
  val version: String,
  val gender: Gender?,
  val dateOfBirth: LocalDate?,
  val dateOfCurrentConviction: LocalDate?,
  val dateAtStartOfFollowup: LocalDate?,
  val totalNumberOfSanctions: Int?,
  val ageAtFirstSanction: Int?,
  val currentOffence: String?,
)
