package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class OffenceCodeDetails(
  val parentGroupDescription: String,
  val categoryDescription: String,
  val subCategoryDescription: String,
  val actuarialCategory: ActuarialCategory,
  val flags: OffenceCodeFlags,
)
