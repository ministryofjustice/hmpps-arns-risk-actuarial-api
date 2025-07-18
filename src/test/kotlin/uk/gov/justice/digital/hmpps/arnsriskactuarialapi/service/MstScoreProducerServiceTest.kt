package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MstInput
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MstScoreProducerServiceTest {

  @InjectMocks
  lateinit var mstScoreProducerService: MstScoreProducerService

  @Test
  fun `should return valid MstObject for valid input`() {
    // When
    val result = mstScoreProducerService.getMstScore(validMstInput())

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
    val maturityFlagFalseInput = validMstInput().copy(attitudesTowardsSelf = ProblemLevel.NO_PROBLEMS)

    // When
    val result = mstScoreProducerService.getMstScore(maturityFlagFalseInput)

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
    val input = MstInput(
      "1_0",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    )

    // When
    val result = mstScoreProducerService.getMstScore(input)

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
        "Impusilvity behaviour",
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
    val isMstApplicableFalseInput = validMstInput().copy(dateOfBirth = LocalDate.now().minusYears(30))
    // When
    val result = mstScoreProducerService.getMstScore(isMstApplicableFalseInput)

    // Then
    assertNotNull(result)
    assertEquals("1_0", result.algorithmVersion)
    assertEquals(null, result.maturityScore)
    assertEquals(null, result.maturityFlag)
    assertEquals(null, result.isMstApplicable)
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

  private fun validMstInput(): MstInput = MstInput(
    "1_0",
    Gender.MALE,
    LocalDate.now().minusYears(18),
    true,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
    ProblemLevel.SOME_PROBLEMS,
  )
}
