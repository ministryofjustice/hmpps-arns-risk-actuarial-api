package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MstInput
import java.time.LocalDate

class MstValidationHelperTest {

  @Test
  fun `should return list of ValidationErrorResponse with MISSING_INPUT validationError`() {
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
        "Attitudes towards self",
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
    val input = MstInput(
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

    // When
    val result = mstInitialValidation(input)
    assertNotNull(result)
    assertTrue(result.isEmpty())
  }
}
