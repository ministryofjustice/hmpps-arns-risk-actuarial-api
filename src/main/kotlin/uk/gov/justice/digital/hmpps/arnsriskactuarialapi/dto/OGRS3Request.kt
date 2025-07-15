package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

import jakarta.validation.constraints.NotBlank
import java.time.LocalDate

data class OGRS3Request(

  val algorithmVersion: AlgorithmVersion,

  @field:NotBlank(message = "gender must not be blank")
  val gender: Gender,

  @field:NotBlank(message = "dateOfBirth must not be blank")
  val dateOfBirth: LocalDate,

  @field:NotBlank(message = "dateOfCurrentConviction must not be blank")
  val dateOfCurrentConviction: LocalDate,

  @field:NotBlank(message = "dateAtStartOfFollowup must not be blank")
  val dateAtStartOfFollowup: LocalDate,

  @field:NotBlank(message = "totalNumberOfSanctions must not be blank")
  val totalNumberOfSanctions: Int,

  @field:NotBlank(message = "ageAtFirstSanction must not be blank")
  val ageAtFirstSanction: Int,

  @field:NotBlank(message = "currentOffence must not be blank")
  val currentOffence: String,
)
