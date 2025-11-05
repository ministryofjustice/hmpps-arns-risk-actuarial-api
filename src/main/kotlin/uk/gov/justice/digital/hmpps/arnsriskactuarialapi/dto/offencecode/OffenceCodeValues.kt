package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class OffenceCodeValues (
  val ogrs3Weighting: Double?,
  val snsvStaticWeighting: Double?,
  val snsvDynamicWeighting: Double?,
  val snsvVatpStaticWeighting: Double?,
  val snsvVatpDynamicWeighting: Double?,
  val opdViolenceSexFlag: Boolean?,
)