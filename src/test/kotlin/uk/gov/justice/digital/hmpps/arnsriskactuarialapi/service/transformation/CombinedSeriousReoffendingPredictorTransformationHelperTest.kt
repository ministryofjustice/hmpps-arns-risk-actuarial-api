package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getFemaleWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.CombinedSeriousReoffendingPredictorTransformationHelper.getScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal
import java.util.stream.Stream
import kotlin.test.assertNull

class CombinedSeriousReoffendingPredictorTransformationHelperTest {

  @Test
  fun `should return female weight when conditions met`() {
    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest().copy(
      gender = Gender.FEMALE,
      hasEverCommittedSexualOffence = true,
    )
    assertEquals(BigDecimal(0.0038314176245211), getFemaleWeight(request))
  }

  @Test
  fun `should return null when female conditions not met`() {
    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest().copy(
      gender = Gender.MALE,
    )
    assertEquals(null, getFemaleWeight(request))
  }

  @Test
  fun `should get null band for null CSRP score`() {
    assertNull(getBand(null))
  }

  @Test
  fun `should get NOT_APPLICABLE band for score less than 0 CSRP score`() {
    assertEquals(RiskBand.NOT_APPLICABLE, getBand(-1.0))
  }

  @Test
  fun `should return LOW for CSRP score within 0 or less than 1`() {
    assertEquals(RiskBand.LOW, getBand(0.0))
    assertEquals(RiskBand.LOW, getBand(0.99))
  }

  @Test
  fun `should return MEDIUM for CSRP score within 1 or less than 3`() {
    assertEquals(RiskBand.MEDIUM, getBand(1.0))
    assertEquals(RiskBand.MEDIUM, getBand(2.99))
  }

  @Test
  fun `should return HIGH for CSRP score within 3 or less than 6 point 9`() {
    assertEquals(RiskBand.HIGH, getBand(3.0))
    assertEquals(RiskBand.HIGH, getBand(6.89))
  }

  @Test
  fun `should return VERY_HIGH for CSRP score when 6 point 9 +`() {
    assertEquals(RiskBand.VERY_HIGH, getBand(6.9))
    assertEquals(RiskBand.VERY_HIGH, getBand(99.99))
  }

  @ParameterizedTest
  @MethodSource("csrpScoreProvider")
  fun `getCSRPScore should calculate correct score`(
    seriousViolent: Double?,
    directContactSexualReoffendingPredictor: Double?,
    iicsrp: Double?,
    femaleAddition: Double?,
    expected: Double?,
  ) {
    val actual = getScore(seriousViolent, directContactSexualReoffendingPredictor, iicsrp, femaleAddition)
    assertEquals(expected, actual)
  }

  companion object {
    @JvmStatic
    fun csrpScoreProvider(): Stream<Arguments> = Stream.of(
      // null case
      Arguments.of(
        null,
        null,
        null,
        null,
        null,
      ),
      // Standard summation
      Arguments.of(
        10.00,
        5.00,
        2.50,
        0.0,
        17.50,
      ),
      // Null female contribution
      Arguments.of(
        10.00,
        5.00,
        2.50,
        null,
        17.50,
      ),
      // Cap score at 99.99
      Arguments.of(
        50.00,
        30.00,
        30.00,
        0.0,
        99.99,
      ),
    )
  }
}
