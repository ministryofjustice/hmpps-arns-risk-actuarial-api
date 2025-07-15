package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Ogrs3TransformationHelperTest {

  private val today = LocalDate.of(2025, 1, 1)

  @TestInstance(TestInstance.Lifecycle.PER_CLASS)
  @Nested
  inner class AgeAtCurrentConvictionTest {

    @Test
    fun `ageAtCurrentConviction age should be greater than min value`() {
      val dob = today.minusYears(10)
      val result = ageAtCurrentConviction(dob, today, ageAtFirstSanction = 10)
      assertTrue(result.isSuccess)
      assertEquals(10, result.getOrNull())
    }

    @Test
    fun `ageAtCurrentConviction should round down months`() {
      val dob = today.minusYears(10).minusMonths(3)
      val result = ageAtCurrentConviction(dob, today, ageAtFirstSanction = 10)
      assertTrue(result.isSuccess)
      assertEquals(10, result.getOrNull())
    }

    @Test
    fun `ageAtCurrentConviction age less than min should return error`() {
      val dob = today.minusYears(9)
      val result = ageAtCurrentConviction(dob, today, ageAtFirstSanction = 18)
      assertTrue(result.isFailure)
      assertEquals("Age at current conviction must be at least 10.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `ageAtCurrentConviction null dateOfBirth should return error`() {
      val result = runCatching {
        ageAtCurrentConviction(
          dateOfBirth = LocalDate.parse(null),
          dateOfCurrentConviction = today,
          ageAtFirstSanction = 18,
        )
      }
      assertTrue(result.isFailure)
    }

    @Test
    fun `ageAtFirstSanction greater than current age should return error`() {
      val dob = today.minusYears(15)
      val result = ageAtCurrentConviction(dob, today, ageAtFirstSanction = 20)
      assertTrue(result.isFailure)
      assertEquals(
        "Age at first sanction cannot be greater than age at current conviction.",
        result.exceptionOrNull()?.message,
      )
    }

    @Test
    fun `ageAtCurrentConviction null date inputs should throw error`() {
      val result =
        ageAtCurrentConviction(
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
}