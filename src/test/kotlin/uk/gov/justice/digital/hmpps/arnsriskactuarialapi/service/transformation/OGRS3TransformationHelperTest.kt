package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OGRS3TransformationHelperTest {

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class OffenderCopasScoreTest {

    @Test
    fun `getOffenderCopasScore should throw error when denominator less or equal zero`() {
      val exception = assertThrows(IllegalArgumentException::class.java) {
        getOffenderCopasScore(0, 1, 18)
      }
      assert(exception.message!!.contains("Invalid age values leading to non-positive denominator"))
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class AgeGenderParameterTest {

    @Test
    fun `getOffenderAgeGroup should throw error when under ten`() {
      val exception = assertThrows(IllegalArgumentException::class.java) {
        getAgeGenderScore(9, Gender.MALE)
      }
      assert(exception.message!!.contains("Unhandled age: 9"))
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when male`() {
      val actual = getAgeGenderScore(11, Gender.MALE)
      assertEquals(0.0, actual)
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when female`() {
      val actual = getAgeGenderScore(11, Gender.FEMALE)
      assertEquals(0.785, actual)
    }

    @Test
    fun `getAgeGenderParameter matches OASys spreadsheet`() {
      val actual = getAgeGenderScore(50, Gender.MALE)
      assertEquals(-2.0253, actual)
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class Ogrs3ProbabilityTest {
    @Test
    fun `getOgrs3OneYear with zero input`() {
      val result = getOgrs3OneYear(0.0)
      assertEquals(0.80259, result)
    }

    @Test
    fun `getOgrs3OneYear matches OASys spreadsheet`() {
      val result = getOgrs3OneYear(-2.776835807)
      assertEquals(0.20193, result)
    }

    @Test
    fun `getOgrs3TwoYear matches OASys spreadsheet`() {
      val result = getOgrs3TwoYear(-2.776835807)
      assertEquals(0.34183, result)
    }

    @Test
    fun `getOgrs3TwoYear with zero input`() {
      val result = getOgrs3TwoYear(0.0)
      assertEquals(0.89299, result)
    }

    @Test
    fun `getOgrs3OneYear with valid input`() {
      val input = 1.0
      val result = getOgrs3OneYear(input)
      assertEquals(0.91702, result)
    }

    @Test
    fun `getOgrs3TwoYear throws for too big values`() {
      assertThrows(IllegalArgumentException::class.java) {
        getOgrs3OneYear(1000.0)
      }
    }

    @Test
    fun `getOgrs3TwoYear throws for too small values`() {
      assertThrows(IllegalArgumentException::class.java) {
        getOgrs3OneYear(-1000.0)
      }
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class GetRiskBandTest {

    @Test
    fun `getRiskBand returns LOW for percentages 0 to 49`() {
      assertEquals(RiskBand.LOW, getRiskBand(0))
      assertEquals(RiskBand.LOW, getRiskBand(49))
    }

    @Test
    fun `getRiskBand returns MEDIUM for percentages 50 to 74`() {
      assertEquals(RiskBand.MEDIUM, getRiskBand(50))
      assertEquals(RiskBand.MEDIUM, getRiskBand(74))
    }

    @Test
    fun `getRiskBand returns HIGH for percentages 75 to 89`() {
      assertEquals(RiskBand.HIGH, getRiskBand(75))
      assertEquals(RiskBand.HIGH, getRiskBand(89))
    }

    @Test
    fun `getRiskBand returns VERY_HIGH for percentages 90 and above`() {
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(90))
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(100))
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(120)) // > 100%
    }

    @Test
    fun `getRiskBand throws exception for negative input`() {
      assertThrows(IllegalArgumentException::class.java) {
        getRiskBand(-1)
      }
    }
  }
}
