package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.noSexualOffenceSanctions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.oneContactChildSexualSanctions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.oneIIOCSanction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.twoOrMoreContactChildSexualSanctions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.twoOrMoreIIOCSanctions

enum class OSPIICHierarchyBand(
  val rsrContribution: Double,
  val band: RiskBand,
  val isMatchFor: (OSPIICInputValidated) -> Boolean,
) {
  TwoOrMoreIIOCSanctions(0.10310, RiskBand.VERY_HIGH, twoOrMoreIIOCSanctions),
  OneIIOCSanction(0.03328, RiskBand.HIGH, oneIIOCSanction),
  TwoOrMoreContactChildSexualSanctions(0.00926, RiskBand.MEDIUM, twoOrMoreContactChildSexualSanctions),
  OneContactChildSexualSanctions(0.00634, RiskBand.LOW, oneContactChildSexualSanctions),
  NoSexualOffenceSanctions(0.00062, RiskBand.LOW, noSexualOffenceSanctions),
  AllOthers(0.00281, RiskBand.LOW, { _ -> true }),
}
