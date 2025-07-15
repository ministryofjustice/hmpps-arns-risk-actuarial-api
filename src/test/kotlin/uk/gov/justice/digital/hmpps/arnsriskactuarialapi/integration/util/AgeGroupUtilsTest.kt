package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.integration.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.AgeGroup
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getAgeGenderParameter
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util.getAgeGroup

class AgeGroupUtilsTest {

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