package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest

class MSTValidationHelperTest {

  private val errors = arrayListOf<ValidationErrorResponse>()

  @Test
  fun `mstInitialValidation should return list of ValidationErrorResponse with MISSING_INPUT validationError`() {
    // Given
    val input = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      gender = null,
      dateOfBirth = null,
      peerGroupInfluences = null,
      attitudesPeerPressure = null,
      attitudesStableBehaviour = null,
      difficultiesCoping = null,
      attitudesTowardsSelf = null,
      impulsivityBehaviour = null,
      temperControl = null,
      problemSolvingSkills = null,
      awarenessOfConsequences = null,
      understandsPeoplesViews = null,
    )

    // When
    val result = mstInitialValidation(input)

    // Then
    val expectedError = ValidationErrorResponse(
      ValidationErrorType.MISSING_INPUT,
      "ERR5 - Field is Null",
      listOf(
        "gender",
        "dateOfBirth",
        "peerGroupInfluences",
        "attitudesPeerPressure",
        "attitudesStableBehaviour",
        "difficultiesCoping",
        "attitudesTowardsSelf",
        "impulsivityBehaviour",
        "temperControl",
        "problemSolvingSkills",
        "awarenessOfConsequences",
        "understandsPeoplesViews",
      ),
    )

    assertNotNull(result)
    assertTrue(result.size == 1)
    assertEquals(expectedError, result.first())
  }

  @Test
  fun `mstInitialValidation should return empty list of ValidationErrorResponse`() {
    // When
    val result = mstInitialValidation(validMSTRiskScoreRequest())

    // Then
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `mstInitialValidation should return empty list of ValidationErrorResponse when only 1 null answer`() {
    // When
    val result = mstInitialValidation(
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
    val result = mstInitialValidation(
      validMSTRiskScoreRequest().copy(
        difficultiesCoping = null,
        temperControl = null,
      ),
    )
    val expectedError = ValidationErrorResponse(
      ValidationErrorType.MISSING_INPUT,
      "ERR5 - Field is Null",
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
  fun `genderAndAgeValidation should return NOT_APPLICABLE validationError when out of age range`() {
    // When
    val result = genderAndAgeValidation(Gender.MALE, 26, errors)

    // Then
    val expectedError = ValidationErrorResponse(
      ValidationErrorType.NOT_APPLICABLE,
      "ERR1 - Does not meet eligibility criteria",
      listOf("dateOfBirth"),
    )

    assertEquals(expectedError, result.first())
  }

  @Test
  fun `genderAndAgeValidation should return NOT_APPLICABLE validationError when FEMALE`() {
    // When
    val result = genderAndAgeValidation(Gender.FEMALE, 24, errors)

    // Then
    val expectedError = ValidationErrorResponse(
      ValidationErrorType.NOT_APPLICABLE,
      "ERR1 - Does not meet eligibility criteria",
      listOf("gender"),
    )

    assertEquals(expectedError, result.first())
  }

  @Test
  fun `genderAndAgeValidation should return NOT_APPLICABLE validationError when FEMALE and out of age range`() {
    // When
    val result = genderAndAgeValidation(Gender.FEMALE, 26, errors)

    // Then
    val expectedError = ValidationErrorResponse(
      ValidationErrorType.NOT_APPLICABLE,
      "ERR1 - Does not meet eligibility criteria",
      listOf("gender", "dateOfBirth"),
    )

    assertEquals(expectedError, result.first())
  }

  @Test
  fun `genderAndAgeValidation should return empty list of ValidationErrorResponse`() {
    // When
    val result = genderAndAgeValidation(Gender.MALE, 25, errors)

    // Then
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
