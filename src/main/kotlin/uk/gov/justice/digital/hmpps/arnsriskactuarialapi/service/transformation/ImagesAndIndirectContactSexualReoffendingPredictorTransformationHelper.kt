package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.ONE_CONTACT_CHILD_SEXUAL_SANCTION
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.asDoublePercentage
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sanitisePercentage
import java.math.BigDecimal

object ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper {
  fun getHierarchyWeight(
    gender: Gender,
    totalIndecentImageSanctions: Int,
    totalContactAdultSexualSanctions: Int,
    totalContactChildSexualSanctions: Int,
    totalNonContactSexualOffences: Int,
  ): BigDecimal {
    if (gender == Gender.FEMALE) return BigDecimal.ZERO

    return when {
      totalIndecentImageSanctions >= 2 -> TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient
      totalIndecentImageSanctions == 1 -> ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION.coefficient
      totalContactChildSexualSanctions >= 2 -> TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS.coefficient
      totalContactChildSexualSanctions == 1 -> ONE_CONTACT_CHILD_SEXUAL_SANCTION.coefficient
      totalNonContactSexualOffences != 0 && totalContactAdultSexualSanctions != 0 -> NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient
      else -> BigDecimal.ZERO
    }
  }

  fun calculatePercentageScore(imagesAndIndirectContactWeight: BigDecimal): Double = imagesAndIndirectContactWeight.toDouble().asDoublePercentage().sanitisePercentage()

  fun getRiskBand(hierarchyWeight: BigDecimal): RiskBand = when (hierarchyWeight) {
    TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient -> RiskBand.HIGH
    ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION.coefficient -> RiskBand.MEDIUM
    else -> RiskBand.LOW
  }
}
