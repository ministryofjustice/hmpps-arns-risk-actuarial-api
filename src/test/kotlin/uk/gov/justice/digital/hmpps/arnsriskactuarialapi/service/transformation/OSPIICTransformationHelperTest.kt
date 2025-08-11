package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICHierarchyBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.OSPIICTransformationHelper.Companion.ospiicHierarchyBand
import java.util.stream.Stream

class OSPIICTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("findOSPIICHierarchyBandsTestProvider")
  fun `find OSPIIC hierarchy bands`(input: OSPIICInputValidated, expected: OSPIICHierarchyBand) {
    val actual = ospiicHierarchyBand(input)
    assertEquals(expected, actual)
  }

  companion object {
    @JvmStatic
    fun findOSPIICHierarchyBandsTestProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        OSPIICInputValidated(66, 2, 4, 65),
        OSPIICHierarchyBand.TwoOrMoreIIOCSanctions,
      ),
      Arguments.of(
        OSPIICInputValidated(66, 2, 1, 65),
        OSPIICHierarchyBand.OneIIOCSanction,
      ),
      Arguments.of(
        OSPIICInputValidated(66, 2, 0, 65),
        OSPIICHierarchyBand.TwoOrMoreContactChildSexualSanctions,
      ),
      Arguments.of(
        OSPIICInputValidated(66, 1, 0, 65),
        OSPIICHierarchyBand.OneContactChildSexualSanctions,
      ),
      Arguments.of(
        OSPIICInputValidated(0, 0, 0, 0),
        OSPIICHierarchyBand.NoSexualOffenceSanctions,
      ),
      Arguments.of(
        OSPIICInputValidated(66, 0, 0, 65),
        OSPIICHierarchyBand.AllOthers,
      ),
    )
  }
}
