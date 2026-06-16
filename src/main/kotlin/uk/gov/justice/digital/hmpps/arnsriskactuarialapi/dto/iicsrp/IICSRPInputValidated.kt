package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp

data class IICSRPInputValidated(
  val totalContactAdultSexualSanctions: Int,
  val totalContactChildSexualSanctions: Int,
  val totalIndecentImageSanctions: Int,
  val totalNonContactSexualOffences: Int,
)
