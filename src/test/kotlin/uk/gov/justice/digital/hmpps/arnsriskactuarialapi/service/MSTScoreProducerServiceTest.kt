package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MSTScoreProducerServiceTest {

  @InjectMocks
  lateinit var mstRiskProducerService: MSTRiskProducerService

  @Test
  fun `should return valid MstObject for valid input`() {
    // When
    val result = mstRiskProducerService.getRiskScore(validMSTRiskScoreRequest())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(10, result.maturityScore)
    assertEquals(true, result.maturityFlag)
    assertEquals(true, result.isMstApplicable)
    assertTrue(result.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid MstObject for valid input where maturityFlag false`() {
    // Given
    val maturityFlagFalseInput = validMSTRiskScoreRequest().copy(attitudesTowardsSelf = ProblemLevel.NO_PROBLEMS)

    // When
    val result = mstRiskProducerService.getRiskScore(maturityFlagFalseInput)

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(9, result.maturityScore)
    assertEquals(false, result.maturityFlag)
    assertEquals(true, result.isMstApplicable)
    assertTrue(result.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid MstObject with MISSING_INPUT validationError`() {
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
    val result = mstRiskProducerService.getRiskScore(input)

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(null, result.maturityScore)
    assertEquals(null, result.maturityFlag)
    assertEquals(null, result.isMstApplicable)
    assertTrue(result.validationError?.size == 1)

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
        "Impulsivity behaviour",
        "Temper control",
        "Problem solving skills",
        "Attitudes towards self",
        "Aweness of consequences",
        "Understands peoples views",
      ),
    )
    val actualError = result.validationError

    assertTrue(actualError?.size == 1)
    assertEquals(expectedError, actualError?.first())
  }

  @Test
  fun `should return valid MstObject with NOT_APPLICABLE validationError when isMstApplicable is false`() {
    val isMstApplicableFalseInput = validMSTRiskScoreRequest().copy(dateOfBirth = LocalDate.now().minusYears(30))
    // When
    val result = mstRiskProducerService.getRiskScore(isMstApplicableFalseInput)

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(null, result.maturityScore)
    assertEquals(null, result.maturityFlag)
    assertEquals(false, result.isMstApplicable)
    assertTrue(result.validationError?.size == 1)

    val expectedError = ValidationErrorResponse(
      ValidationErrorType.NOT_APPLICABLE,
      "ERR - Does not meet eligibility criteria",
      listOf("Gender", "Date of birth"),
    )
    val actualError = result.validationError

    assertTrue(actualError?.size == 1)
    assertEquals(expectedError, actualError?.first())
  }
}
