package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode

data class OffenceCodeValues(
  val ogrs3Weighting: OffenceCodeWeighting,
  val snsvStaticWeighting: OffenceCodeWeighting,
  val snsvDynamicWeighting: OffenceCodeWeighting,
  val snsvVatpStaticWeighting: OffenceCodeWeighting,
  val snsvVatpDynamicWeighting: OffenceCodeWeighting,
  val opdViolenceSexFlag: Boolean?,
)
