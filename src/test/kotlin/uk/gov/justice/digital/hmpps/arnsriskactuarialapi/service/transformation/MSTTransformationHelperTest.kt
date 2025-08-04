package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate
import kotlin.test.assertFalse

class MSTTransformationHelperTest {

  private val today = FIXED_TEST_DATE

  @Test
  fun `roundedAge should return correct age`() {
    val dob = today.minusYears(30)
    val result = roundedAge(dob, today)
    assertEquals(30, result)
  }

  @Test
  fun `roundedAge should return ignore days of month`() {
    val dob = LocalDate.of(2007, 7, 10)
    val result = roundedAge(dob, LocalDate.of(2025, 7, 8))
    assertEquals(18, result)
  }

  @Test
  fun `getMstApplicable should return true for age limitations`() {
    val resultLowerLimit = getMstApplicable(Gender.MALE, 18)
    assertTrue(resultLowerLimit)

    val resultUpperLimit = getMstApplicable(Gender.MALE, 24)
    assertTrue(resultUpperLimit)
  }

  @Test
  fun `getMstApplicable should return false for age and gender limitations`() {
    val resultLowerLimit = getMstApplicable(Gender.MALE, 17)
    assertFalse(resultLowerLimit)

    val resultUpperLimit = getMstApplicable(Gender.MALE, 26)
    assertFalse(resultUpperLimit)

    val resultForFemale = getMstApplicable(Gender.FEMALE, 18)
    assertFalse(resultForFemale)
  }

  @Test
  fun `getMaturityFlag should return true when maturityScore is 10 or more`() {
    val result = getMaturityFlag(10)
    assertTrue(result)
  }

  @Test
  fun `getMaturityFlag should return false when maturityScore less than 10`() {
    val result = getMaturityFlag(9)
    assertFalse(result)
  }
}
