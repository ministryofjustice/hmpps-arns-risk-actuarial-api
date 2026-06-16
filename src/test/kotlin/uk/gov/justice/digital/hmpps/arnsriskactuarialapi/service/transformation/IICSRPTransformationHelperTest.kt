package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import IICSRPTransformationHelper.iicsrpHierarchy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp.IICSRPHierarchy
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp.IICSRPInputValidated
import java.util.stream.Stream

class IICSRPTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("findIICSRPHierarchyTestProvider")
  fun `find IICSRP hierarchy`(input: IICSRPInputValidated, expected: IICSRPHierarchy) {
    val actual = iicsrpHierarchy(input)
    assertEquals(expected, actual)
  }

  companion object {
    @JvmStatic
    fun findIICSRPHierarchyTestProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        IICSRPInputValidated(66, 2, 4, 65),
        IICSRPHierarchy.TwoOrMoreIICSanctions,
      ),
      Arguments.of(
        IICSRPInputValidated(66, 2, 1, 65),
        IICSRPHierarchy.OneIICSanction,
      ),
      Arguments.of(
        IICSRPInputValidated(66, 2, 0, 65),
        IICSRPHierarchy.TwoOrMoreContactChildSexualSanctions,
      ),
      Arguments.of(
        IICSRPInputValidated(66, 1, 0, 65),
        IICSRPHierarchy.OneContactChildSexualSanctions,
      ),
      Arguments.of(
        IICSRPInputValidated(0, 0, 0, 0),
        IICSRPHierarchy.NoSexualOffenceSanctions,
      ),
      Arguments.of(
        IICSRPInputValidated(66, 0, 0, 65),
        IICSRPHierarchy.AllOthers,
      ),
    )
  }
}
