package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest

class MSTValidationHelperTest {

  @Test
  fun `should return list of ValidationErrorResponse with MISSING_INPUT validationError`() {
    // Given
    val input = RiskScoreRequest(
      version = "1_0",
      gender = null,
      dateOfBirth = null,
      peerGroupInfluences = null,
      attitudesPeerPressure = null,
      attitudesStableBehaviour = null,
      difficultiesCoping = null,
      attitudesTowardsSelf = null,
      impusilvityBehaviour = null,
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
        "Gender",
        "Date of birth",
        "Peer group influences",
        "Attitudes peer pressure",
        "Attitudes stable behaviour",
        "Difficulties coping",
        "Attitudes towards self",
        "Impusilvity behaviour",
        "Temper control",
        "Problem solving skills",
        "Aweness of consequences",
        "Understands peoples views",
      ),
    )

    assertNotNull(result)
    assertTrue(result.size == 1)
    assertEquals(expectedError, result.first())
  }

  @Test
  fun `should return empty list of ValidationErrorResponse`() {
    val result = mstInitialValidation(validMSTRiskScoreRequest())
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
