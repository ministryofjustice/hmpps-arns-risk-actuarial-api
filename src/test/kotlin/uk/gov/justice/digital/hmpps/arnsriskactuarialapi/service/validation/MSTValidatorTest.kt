package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest
import kotlin.test.assertFalse

class MSTValidatorTest {

  private val validator = MSTValidator()

  @Test
  fun `mstInitialValidation should return list of ValidationErrorResponse with MISSING_INPUT validationError`() {
    // Given
    val input = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      gender = null,
      dateOfBirth = null,
      hasPeerGroupInfluences = null,
      influenceFromCriminalAssociates = null,
      recklessnessAndRiskTakingBehaviour = null,
      difficultiesCoping = null,
      attitudesTowardsSelf = null,
      impulsivityProblems = null,
      temperControl = null,
      problemSolvingSkills = null,
      awarenessOfConsequences = null,
      understandsOtherPeoplesViews = null,
    )

    // When
    val result = validator.validateMST(input)

    // Then
    val expectedError = ValidationError(
      ValidationErrorType.MISSING_MANDATORY_INPUT,
      "Mandatory input field(s) missing",
      listOf(
        "gender",
        "dateOfBirth",
        "hasPeerGroupInfluences",
        "influenceFromCriminalAssociates",
        "recklessnessAndRiskTakingBehaviour",
        "difficultiesCoping",
        "attitudesTowardsSelf",
        "impulsivityProblems",
        "temperControl",
        "problemSolvingSkills",
        "awarenessOfConsequences",
        "understandsOtherPeoplesViews",
      ),
    )

    assertNotNull(result)
    assertTrue(result.size == 1)
    assertEquals(expectedError, result.first())
  }

  @Test
  fun `mstInitialValidation should return empty list of ValidationErrorResponse`() {
    // When
    val result = validator.validateMST(validMSTRiskScoreRequest())

    // Then
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `mstInitialValidation should return empty list of ValidationErrorResponse when only 1 null answer`() {
    // When
    val result = validator.validateMST(
      validMSTRiskScoreRequest().copy(
        difficultiesCoping = null,
      ),
    )

    // Then
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `mstInitialValidation should return a list of ValidationErrorResponse when 2 null answer`() {
    // When
    val result = validator.validateMST(
      validMSTRiskScoreRequest().copy(
        difficultiesCoping = null,
        temperControl = null,
      ),
    )
    val expectedError = ValidationError(
      ValidationErrorType.MISSING_MANDATORY_INPUT,
      "Mandatory input field(s) missing",
      listOf(
        "difficultiesCoping",
        "temperControl",
      ),
    )

    assertNotNull(result)
    assertTrue(result.size == 1)
    assertEquals(expectedError, result.first())
  }

  @Test
  fun `getMstApplicable should return true for age limitations`() {
    val resultLowerLimit = validator.getMstApplicable(Gender.MALE, 18)
    assertTrue(resultLowerLimit)

    val resultUpperLimit = validator.getMstApplicable(Gender.MALE, 24)
    assertTrue(resultUpperLimit)
  }

  @Test
  fun `getMstApplicable should return false for age and gender limitations`() {
    val resultLowerLimit = validator.getMstApplicable(Gender.MALE, 17)
    assertFalse(resultLowerLimit)

    val resultUpperLimit = validator.getMstApplicable(Gender.MALE, 26)
    assertFalse(resultUpperLimit)

    val resultForFemale = validator.getMstApplicable(Gender.FEMALE, 18)
    assertFalse(resultForFemale)
  }

  @Test
  fun `isNotNullAndInvalidMstAge should return false for null and within range age`() {
    val resultNullAge = validator.isNotNullAndInvalidMstAge(null)
    assertFalse(resultNullAge)

    val resultWithinValidRange = validator.isNotNullAndInvalidMstAge(22)
    assertFalse(resultWithinValidRange)
  }

  @Test
  fun `isNotNullAndInvalidMstAge should return true for out of range age`() {
    val resultOutOfValidRange = validator.isNotNullAndInvalidMstAge(30)
    assertTrue(resultOutOfValidRange)
  }
}
