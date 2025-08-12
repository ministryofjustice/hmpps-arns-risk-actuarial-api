package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic

data class OSPIICInputValidated(
  val totalContactAdultSexualSanctions: Int,
  val totalContactChildSexualSanctions: Int,
  val totalIndecentImageSanctions: Int,
  val totalNonContactSexualOffences: Int,
)
