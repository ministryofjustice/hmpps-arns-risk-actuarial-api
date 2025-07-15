package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.AgeGroup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.math.BigDecimal
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Ogrs3TransformationHelperTest {

  private val today = LocalDate.of(2025, 1, 1)

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class AgeAtCurrentConvictionTest {

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
    fun `getAgeAtCurrentConviction null date inputs should throw error`() {
      val result =
        getAgeAtCurrentConviction(
          dateOfBirth = today.minusYears(18),
          dateOfCurrentConviction = null,
          ageAtFirstSanction = 18,
        )

      assertTrue(result.isFailure)
      assertEquals(
        "conviction date is null.",
        result.exceptionOrNull()?.message,
      )
    }
  }

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class OffenderCopasScoreTest {
    @Test
    fun `getOffenderCopasScore should produce the copas score`() {
      val score = getOffenderCopasScore(3, 30, 20)
      val expected = BigDecimal("-1.60944") // TODO: find some test data to compare against
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
    fun `getAgeGroup should return age group`() {
      val actual = getAgeGroup(10)
      assertEquals(AgeGroup.TEN_TO_UNDER_TWELVE, actual)
    }

    @Test
    fun `getAgeGroup should throw error when under ten`() {
      val exception = assertThrows(IllegalArgumentException::class.java) {
        getAgeGroup(9)
      }
      assert(exception.message!!.contains("Age must be age 10 or more"))
    }

    @Test
    fun `getAgeGroup should throw error when negative value`() {
      val exception = assertThrows(IllegalArgumentException::class.java) {
        getAgeGroup(-1)
      }
      assert(exception.message!!.contains("Age cannot be negative"))
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when male`() {
      val actual = getAgeGenderParameter(AgeGroup.TEN_TO_UNDER_TWELVE, Gender.MALE)
      assertEquals(0.0, actual)
    }

    @Test
    fun `getAgeGenderParameter should return the current weight for the age group when female`() {
      val actual = getAgeGenderParameter(AgeGroup.TEN_TO_UNDER_TWELVE, Gender.FEMALE)
      assertEquals(0.785, actual)
    }
  }
}
