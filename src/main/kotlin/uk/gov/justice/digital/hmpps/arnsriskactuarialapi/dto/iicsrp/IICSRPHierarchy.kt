package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

enum class IICSRPHierarchy(
  val csrpContribution: Double,
  val band: RiskBand,
) {
  TwoOrMoreIICSanctions(0.10310 * 100, RiskBand.HIGH),
  OneIICSanction(0.03328 * 100, RiskBand.MEDIUM),
  TwoOrMoreContactChildSexualSanctions(0.00926 * 100, RiskBand.LOW),
  OneContactChildSexualSanctions(0.00634 * 100, RiskBand.LOW),
  NoSexualOffenceSanctions(0.0, RiskBand.LOW),
  AllOthers(0.00281 * 100, RiskBand.LOW),
}
