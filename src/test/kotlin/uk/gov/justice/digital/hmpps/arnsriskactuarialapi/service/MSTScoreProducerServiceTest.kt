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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMSTRiskScoreRequest
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MSTScoreProducerServiceTest {

  @InjectMocks
  lateinit var mstRiskProducerService: MSTRiskProducerService

  @Test
  fun `should return valid MstObject for valid input`() {
    // When
    val result = mstRiskProducerService.getRiskScore(validMSTRiskScoreRequest(), emptyContext())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.MST!!.algorithmVersion)
    assertEquals(10, result.MST.maturityScore)
    assertEquals(true, result.MST.maturityFlag)
    assertEquals(true, result.MST.isMstApplicable)
    assertTrue(result.MST.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid MstObject for valid input where maturityFlag false`() {
    // Given
    val maturityFlagFalseInput = validMSTRiskScoreRequest().copy(attitudesTowardsSelf = ProblemLevel.NO_PROBLEMS)

    // When
    val result = mstRiskProducerService.getRiskScore(maturityFlagFalseInput, emptyContext())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.MST!!.algorithmVersion)
    assertEquals(9, result.MST.maturityScore)
    assertEquals(false, result.MST.maturityFlag)
    assertEquals(true, result.MST.isMstApplicable)
    assertTrue(result.MST.validationError.isNullOrEmpty())
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
    val result = mstRiskProducerService.getRiskScore(input, emptyContext())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.MST!!.algorithmVersion)
    assertEquals(null, result.MST.maturityScore)
    assertEquals(null, result.MST.maturityFlag)
    assertEquals(null, result.MST.isMstApplicable)
    assertTrue(result.MST.validationError?.size == 1)

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
        "Aweness of consequences",
        "Understands peoples views",
      ),
    )
    val actualError = result.MST.validationError

    assertTrue(actualError?.size == 1)
    assertEquals(expectedError, actualError?.first())
  }

  @Test
  fun `should return valid MstObject with NOT_APPLICABLE validationError when isMstApplicable is false`() {
    val isMstApplicableFalseInput = validMSTRiskScoreRequest().copy(dateOfBirth = LocalDate.now().minusYears(30))
    // When
    val result = mstRiskProducerService.getRiskScore(isMstApplicableFalseInput, emptyContext())

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.MST!!.algorithmVersion)
    assertEquals(null, result.MST.maturityScore)
    assertEquals(null, result.MST.maturityFlag)
    assertEquals(false, result.MST.isMstApplicable)
    assertTrue(result.MST.validationError?.size == 1)

    val expectedError = ValidationErrorResponse(
      ValidationErrorType.NOT_APPLICABLE,
      "ERR - Does not meet eligibility criteria",
      listOf("Gender", "Date of birth"),
    )
    val actualError = result.MST.validationError

    assertTrue(actualError?.size == 1)
    assertEquals(expectedError, actualError?.first())
  }
}
