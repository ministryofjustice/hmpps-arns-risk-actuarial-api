package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import java.time.LocalDate

data class OGRS3Request(
  val algorithmVersion: AlgorithmVersion,
  val gender: Gender?,
  val dateOfBirth: LocalDate?,
  val dateOfCurrentConviction: LocalDate?,
  val dateAtStartOfFollowup: Int?,
  val totalNumberOfSanctions: Int?,
  val ageAtFirstSanction: Int?,
  val currentOffence: String?
)

