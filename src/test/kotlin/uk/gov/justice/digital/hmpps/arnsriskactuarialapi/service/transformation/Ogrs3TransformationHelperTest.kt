package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Ogrs3TransformationHelperTest {

  private val today = LocalDate.of(2025, 1, 1)

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class AgeTest {

    @Test
    fun `getAgeAtCurrentConviction age should be greater than min value`() {
      val dob = today.minusYears(10)
      val result = getAgeAtCurrentConviction(dob, today, ageAtFirstSanction = 10)
      assertTrue(result.isSuccess)
      assertEquals(10, result.getOrNull())
    }

    @Test
    fun `getAgeAtCurrentConviction should round down months`() {
      val dob = today.minusYears(10).minusMonths(3)
      val result = getAgeAtCurrentConviction(dob, today, ageAtFirstSanction = 10)
      assertTrue(result.isSuccess)
      assertEquals(10, result.getOrNull())
    }

    @Test
    fun `getAgeAtCurrentConviction age less than min should return error`() {
      val dob = today.minusYears(9)
      val result = getAgeAtCurrentConviction(dob, today, ageAtFirstSanction = 18)
      assertTrue(result.isFailure)
      assertEquals("Age at current conviction must be at least 10.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getAgeAtCurrentConviction null dateOfBirth should return error`() {
      val result = runCatching {
        getAgeAtCurrentConviction(
          dateOfBirth = LocalDate.parse(null),
          dateOfCurrentConviction = today,
          ageAtFirstSanction = 18,
        )
      }
      assertTrue(result.isFailure)
    }

    @Test
    fun `getAgeAtCurrentConviction greater than current age should return error`() {
      val dob = today.minusYears(15)
      val result = getAgeAtCurrentConviction(dob, today, ageAtFirstSanction = 20)
      assertTrue(result.isFailure)
      assertEquals(
        "Age at first sanction cannot be greater than age at current conviction.",
        result.exceptionOrNull()?.message,
      )
    }

    @Test
    fun `getAgeAtStartOfFollowup exact`() {
      val dob = today.minusYears(15)
      assertEquals(
        15,
        getAgeAtStartOfFollowup(dob, today),
      )
    }

    @Test
    fun `getAgeAtStartOfFollowup round down`() {
      val dob = today.minusYears(15).minusMonths(3)
      assertEquals(
        15,
        getAgeAtStartOfFollowup(dob, today),
      )
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class OffenderCopasScoreTest {
    @Test
    fun `getOffenderCopasScore should produce the copas score`() {
      val score = getOffenderCopasScore(3, 30, 20)
      val expected = -1.60944 // TODO: find some test data to compare against
      assertEquals(expected, score)
    }

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
        getAgeGenderParameter(9, Gender.MALE)
      }
      assert(exception.message!!.contains("Unhandled age: 9"))
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when male`() {
      val actual = getAgeGenderParameter(11, Gender.MALE)
      assertEquals(0.0, actual)
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when female`() {
      val actual = getAgeGenderParameter(11, Gender.FEMALE)
      assertEquals(0.785, actual)
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class Ogrs3ProbabilityTest {
    @Test
    fun `getOgrs3OneYear with zero input`() {
      val result = getOgrs3OneYear(0.0)
      assertEquals(0.80259, result) // TODO: get some examples OASys values to compare
    }

    @Test
    fun `getOgrs3TwoYear with zero input`() {
      val result = getOgrs3TwoYear(0.0)
      assertEquals(0.89299, result) // TODO: get some examples OASys values to compare
    }

    @Test
    fun `getOgrs3OneYear with valid input`() {
      val input = 1.0
      val result = getOgrs3OneYear(input)
      assertEquals(0.91702, result) // TODO: get some examples OASys values to compare
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
      assertEquals(RiskBand.LOW, getRiskBand(0.00))
      assertEquals(RiskBand.LOW, getRiskBand(0.49))
    }

    @Test
    fun `getRiskBand returns MEDIUM for percentages 50 to 74`() {
      assertEquals(RiskBand.MEDIUM, getRiskBand(0.50))
      assertEquals(RiskBand.MEDIUM, getRiskBand(0.74))
    }

    @Test
    fun `getRiskBand returns HIGH for percentages 75 to 89`() {
      assertEquals(RiskBand.HIGH, getRiskBand(0.75))
      assertEquals(RiskBand.HIGH, getRiskBand(0.89))
    }

    @Test
    fun `getRiskBand returns VERY_HIGH for percentages 90 and above`() {
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(0.90))
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(1.00))
      assertEquals(RiskBand.VERY_HIGH, getRiskBand(1.20)) // > 100%
    }

    @Test
    fun `getRiskBand throws exception for negative input`() {
      assertThrows(IllegalArgumentException::class.java) {
        getRiskBand(-0.01)
      }
    }
  }
}
