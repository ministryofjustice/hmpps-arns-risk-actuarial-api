package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.time.LocalDate
import java.util.stream.Stream
import kotlin.test.assertNull

class RSRTransformationHelperTest {

  @Test
  fun `should return LOW for RSR score within 0 point 0 to 3 point 0`() {
    assertEquals(RiskBand.LOW, getRSRBand(0.0))
    assertEquals(RiskBand.LOW, getRSRBand(1.5))
    assertEquals(RiskBand.LOW, getRSRBand(3.0))
  }

  @Test
  fun `should return MEDIUM for RSR score within 3 point 0 to 6 point 8`() {
    assertEquals(RiskBand.MEDIUM, getRSRBand(3.1))
    assertEquals(RiskBand.MEDIUM, getRSRBand(5.5))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.87))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.81))
    assertEquals(RiskBand.MEDIUM, getRSRBand(6.82))
  }

  @Test
  fun `should return HIGH for RSR score 6 point 9 or above`() {
    assertEquals(RiskBand.HIGH, getRSRBand(6.9))
  }

  @Test
  fun `should get null band for null RSR score`() {
    assertNull(getRSRBand(null))
  }

  @Test
  fun `should throw IllegalArgumentException for negative RSR score`() {
    val exception = assertThrows<IllegalArgumentException> {
      getRSRBand(-1.0)
    }
    assertEquals("RSR Score out of supported range: -1.0", exception.message)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCRiskReductionCases")
  fun testGetOSPDCRiskReduction(
    gender: Gender,
    supervisionStatus: CustodyOrCommunity,
    mostRecentOffenceDate: LocalDate?,
    dateOfMostRecentSexualOffence: LocalDate?,
    dateAtStartOfFollowup: LocalDate,
    assessmentDate: LocalDate,
    riskBand: RiskBand?,
    expected: Boolean?,
  ) {
    val result = getOSPDCRiskReduction(
      gender,
      supervisionStatus,
      mostRecentOffenceDate,
      dateOfMostRecentSexualOffence,
      dateAtStartOfFollowup,
      assessmentDate,
      riskBand,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCRiskBandReductionCases")
  fun testGetOSPDCRiskBandReduction(
    ospRiskReduction: Boolean?,
    riskBand: RiskBand?,
    expected: RiskBand?,
  ) {
    val result = getOSPDCRiskBandReduction(ospRiskReduction, riskBand)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOSPDCRiskBandReductionExceptionCases")
  fun testGetOSPDCRiskBandReductionExceptions(
    ospRiskReduction: Boolean?,
    riskBand: RiskBand,
  ) {
    assertThrows(IllegalArgumentException::class.java) {
      getOSPDCRiskBandReduction(ospRiskReduction, riskBand)
    }
  }

  companion object {
    @JvmStatic
    fun getOSPDCRiskReductionCases(): Stream<Arguments> {
      val today = LocalDate.of(2025, 1, 1)

      return Stream.of(
        // Case 1: Null riskBand → expect null
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          null,
          today.minusYears(6),
          today,
          null,
          null,
        ),
        // Case 2: Female → should return false
        Arguments.of(
          Gender.FEMALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          null,
          today.minusYears(10),
          today,
          RiskBand.HIGH,
          false,
        ),
        // Case 3: In custody → should return false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.CUSTODY,
          null,
          null,
          today.minusYears(10),
          today,
          RiskBand.HIGH,
          false,
        ),
        // Case 4: Recent offence within 5 years → false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          today.minusYears(3),
          null,
          today.minusYears(10),
          today,
          RiskBand.HIGH,
          false,
        ),
        // Case 5: Sexual offence within 5 years → false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          today.minusYears(4),
          today.minusYears(10),
          today,
          RiskBand.HIGH,
          false,
        ),
        // Case 6: Follow-up period less than 5 years → false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          null,
          today.minusYears(3),
          today,
          RiskBand.HIGH,
          false,
        ),
        // Case 7: Low risk band → false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          null,
          today.minusYears(10),
          today,
          RiskBand.LOW,
          false,
        ),
        // Case 8: Not applicable risk band → false
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          null,
          null,
          today.minusYears(10),
          today,
          RiskBand.NOT_APPLICABLE,
          false,
        ),
        // Case 9: Meets all conditions (male, community, no recent offences, >5y follow-up, valid riskBand) → true
        Arguments.of(
          Gender.MALE,
          CustodyOrCommunity.COMMUNITY,
          today.minusYears(6),
          today.minusYears(6),
          today.minusYears(6),
          today,
          RiskBand.HIGH,
          true,
        ),
      )
    }

    @JvmStatic
    fun getOSPDCRiskBandReductionCases(): Stream<Arguments> = Stream.of(
      // ospRiskReduction == null → return riskBand
      Arguments.of(null, RiskBand.HIGH, RiskBand.HIGH),

      // ospRiskReduction == false → return riskBand
      Arguments.of(false, RiskBand.MEDIUM, RiskBand.MEDIUM),

      // riskBand == null → return null
      Arguments.of(true, null, null),

      // ospRiskReduction == true, valid reductions
      Arguments.of(true, RiskBand.VERY_HIGH, RiskBand.HIGH),
      Arguments.of(true, RiskBand.HIGH, RiskBand.MEDIUM),
      Arguments.of(true, RiskBand.MEDIUM, RiskBand.LOW),
    )

    @JvmStatic
    fun getOSPDCRiskBandReductionExceptionCases(): Stream<Arguments> = Stream.of(
      Arguments.of(true, RiskBand.LOW),
      Arguments.of(true, RiskBand.NOT_APPLICABLE),
    )
  }
}
