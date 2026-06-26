package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.math.BigDecimal
import java.util.stream.Stream

class CombinedSeriousReoffendingPredictorTransformationHelperTest {

  @Test
  fun `should get NOT_APPLICABLE band for score less than 0 CSRP score`() {
    assertEquals(RiskBand.NOT_APPLICABLE, getCSRPBand(BigDecimal("-1")))
  }

  @Test
  fun `should return LOW for CSRP score within 0 or less than 1`() {
    assertEquals(RiskBand.LOW, getCSRPBand(BigDecimal.ZERO))
    assertEquals(RiskBand.LOW, getCSRPBand(BigDecimal("0.99")))
  }

  @Test
  fun `should return MEDIUM for CSRP score within 1 or less than 3`() {
    assertEquals(RiskBand.MEDIUM, getCSRPBand(BigDecimal.ONE))
    assertEquals(RiskBand.MEDIUM, getCSRPBand(BigDecimal("2.99")))
  }

  @Test
  fun `should return HIGH for CSRP score within 3 or less than 6 point 9`() {
    assertEquals(RiskBand.HIGH, getCSRPBand(BigDecimal("3")))
    assertEquals(RiskBand.HIGH, getCSRPBand(BigDecimal("6.89")))
  }

  @Test
  fun `should return VERY_HIGH for CSRP score when 6 point 9 +`() {
    assertEquals(RiskBand.VERY_HIGH, getCSRPBand(BigDecimal("6.9")))
    assertEquals(RiskBand.VERY_HIGH, getCSRPBand(BigDecimal("99.99")))
  }

  @ParameterizedTest
  @MethodSource("csrpScoreProvider")
  fun `getCSRPScore should calculate correct score`(
    gender: Gender,
    sexualOffendingHistory: Boolean,
    seriousViolent: BigDecimal,
    directContactSexualReoffendingPredictor: BigDecimal,
    iicsrp: BigDecimal,
    expected: BigDecimal,
  ) {
    val actual = getCSRPScore(gender, sexualOffendingHistory, seriousViolent, directContactSexualReoffendingPredictor, iicsrp)
    assertThat(actual).isEqualByComparingTo(expected)
  }

  companion object {
    @JvmStatic
    fun csrpScoreProvider(): Stream<Arguments> = Stream.of(
      // Standard summation
      Arguments.of(
        Gender.MALE,
        false,
        BigDecimal("10.00"),
        BigDecimal("5.00"),
        BigDecimal("2.50"),
        BigDecimal("17.50"),
      ),
      // Female sexual offender receives coefficient
      Arguments.of(
        Gender.FEMALE,
        true,
        BigDecimal("10.00"),
        BigDecimal("5.00"),
        BigDecimal("2.50"),
        BigDecimal("17.50383141762452109992109772207413698197342455387115478515625"),
      ),
      // Cap score at 99.99
      Arguments.of(
        Gender.MALE,
        false,
        BigDecimal("50.00"),
        BigDecimal("30.00"),
        BigDecimal("30.00"),
        BigDecimal("99.99"),
      ),
      // Female non-sexual offender does not get coefficient
      Arguments.of(
        Gender.FEMALE,
        false,
        BigDecimal("10.00"),
        BigDecimal("5.00"),
        BigDecimal("2.50"),
        BigDecimal("17.50"),
      ),
    )
  }
}
