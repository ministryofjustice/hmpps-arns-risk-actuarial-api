package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class HoCodeWeighting(
  val name: String,
  val value: Double?,
  val description: String,
  val errorCode: HoCodeErrorCode?,
)
