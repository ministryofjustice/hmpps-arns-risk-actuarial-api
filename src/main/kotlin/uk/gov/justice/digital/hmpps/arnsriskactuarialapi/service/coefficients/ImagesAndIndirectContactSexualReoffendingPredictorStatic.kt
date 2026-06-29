package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import java.math.BigDecimal

enum class ImagesAndIndirectContactSexualReoffendingPredictorStatic(val label: String, val coefficient: BigDecimal) {

  TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS("twoOrMoreImageAndIndirectContactSanctions", BigDecimal(0.10310)),
  ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION("oneImageAndIndirectContactSanction", BigDecimal(0.03328)),
  TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS("twoOrMoreContactChildSexualSanctions", BigDecimal(0.00926)),
  ONE_CONTACT_CHILD_SEXUAL_SANCTION("oneContactChildSexualSanction", BigDecimal(0.00634)),
  NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS("notImageAndIndirectContactSanction", BigDecimal(0.00281)),
}
