package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class HoCodeWeightings(
  val name: String,
  val value: Double?,
  val description: String,
  val errorCode: HoCodeErrorCode?,
)
