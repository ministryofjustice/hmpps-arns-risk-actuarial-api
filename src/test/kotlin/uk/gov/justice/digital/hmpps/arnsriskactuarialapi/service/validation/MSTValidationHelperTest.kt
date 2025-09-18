package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest

class MSTValidationHelperTest {

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
    val result = validateMST(input)

    // Then
    val expectedError = ValidationErrorResponse(
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
    val result = validateMST(validMSTRiskScoreRequest())

    // Then
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `mstInitialValidation should return empty list of ValidationErrorResponse when only 1 null answer`() {
    // When
    val result = validateMST(
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
    val result = validateMST(
      validMSTRiskScoreRequest().copy(
        difficultiesCoping = null,
        temperControl = null,
      ),
    )
    val expectedError = ValidationErrorResponse(
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
}
