package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.ONE_CONTACT_CHILD_SEXUAL_SANCTION
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ImagesAndIndirectContactSexualReoffendingPredictorStatic.TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper.getHierarchyWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelper.getRiskBand
import java.math.BigDecimal

class ImagesAndIndirectContactSexualReoffendingPredictorTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("getHierarchyWeightProvider")
  fun `getHierarchyWeight returns correct coefficient based on gender and sanctions`(
    gender: Gender,
    totalIndecentImageSanctions: Int,
    totalContactAdultSexualSanctions: Int,
    totalContactChildSexualSanctions: Int,
    totalNonContactSexualOffences: Int,
    expected: BigDecimal,
  ) {
    val result = getHierarchyWeight(
      gender,
      totalIndecentImageSanctions,
      totalContactAdultSexualSanctions,
      totalContactChildSexualSanctions,
      totalNonContactSexualOffences,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getRiskBandProvider")
  fun `getRiskBand returns correct band mapping based on boundaries`(
    hierarchyWeight: BigDecimal,
    expected: RiskBand,
  ) {
    assertEquals(
      expected,
      getRiskBand(hierarchyWeight),
    )
  }

  companion object {
    @JvmStatic
    fun getHierarchyWeightProvider() = listOf(
      Arguments.of(Gender.FEMALE, 1, 1, 1, 1, BigDecimal.ZERO),
      Arguments.of(Gender.MALE, 2, 0, 0, 0, TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient),
      Arguments.of(Gender.MALE, 1, 0, 0, 0, ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION.coefficient),
      Arguments.of(Gender.MALE, 0, 0, 2, 0, TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS.coefficient),
      Arguments.of(Gender.MALE, 0, 0, 1, 0, ONE_CONTACT_CHILD_SEXUAL_SANCTION.coefficient),
      Arguments.of(Gender.MALE, 0, 1, 0, 1, NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient),
    )

    @JvmStatic
    fun getRiskBandProvider() = listOf(
      Arguments.of(TWO_OR_MORE_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient, RiskBand.HIGH),
      Arguments.of(ONE_IMAGE_AND_INDIRECT_CONTACT_SANCTION.coefficient, RiskBand.MEDIUM),
      Arguments.of(TWO_OR_MORE_CONTACT_CHILD_SEXUAL_SANCTIONS.coefficient, RiskBand.LOW),
      Arguments.of(ONE_CONTACT_CHILD_SEXUAL_SANCTION.coefficient, RiskBand.LOW),
      Arguments.of(NO_IMAGE_AND_INDIRECT_CONTACT_SANCTIONS.coefficient, RiskBand.LOW),
      Arguments.of(BigDecimal.ZERO, RiskBand.LOW),
    )
  }
}
