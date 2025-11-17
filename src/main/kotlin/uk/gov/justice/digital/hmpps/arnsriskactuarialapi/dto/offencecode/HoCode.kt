package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class HoCode(
  val category: Int,
  val subCategory: Int,
  val flags: List<HoCodeFlags>,
  val weightings: List<HoCodeWeightings>,
)
