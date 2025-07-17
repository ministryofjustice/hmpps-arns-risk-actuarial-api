package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate

class OVPValidationHelperTest {

  @Test
  fun `getMissingFieldsValidation no errors`() {
    val result = getMissingOVPFieldsValidation(validOVPRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getMissingFieldsValidation missing field error with all field populated`() {
    val request = RiskScoreRequest(
      "1_0",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    val result = getMissingOVPFieldsValidation(request)

    val expectedFields = listOf(
      "Gender",
      "Date of birth",
      "Date at start of followup",
      "Total number of sanctions",
      "Total number of violent sanctions",
      "Impact of offending on others",
      "Current accommodation",
      "Employment status",
      "Alcohol is current use a problem",
      "Alcohol excessive 6 months",
      "Current psychiatric treatment or pending",
      "Temper control",
      "Pro criminal attitudes",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no errors`() {
    val result = getTotalNumberOfSanctionsValidation(1 as Integer?, mutableListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no error added when number of sanctions is null`() {
    val missingFieldError = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OVP score due to missing field(s)",
        fields = listOf("Total number of sanctions"),
      ),
    )
    val result = getTotalNumberOfSanctionsValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.BELOW_MIN_VALUE == result.first().type)
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation below min value error`() {
    val result = getTotalNumberOfSanctionsValidation(0 as Integer?, mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("Total number of sanctions", error.fields?.first())
  }

  @Test
  fun `getCurrentOffenceValidation no errors`() {
    val result = getCurrentOffenceValidation("00101", mutableListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getCurrentOffenceValidation no error added when current offence null`() {
    val missingFieldError = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("Current offence"),
      ),
    )
    val result = getCurrentOffenceValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.NO_MATCHING_INPUT == result.first().type)
  }

  @Test
  fun `getCurrentOffenceValidation char count error`() {
    val result = getCurrentOffenceValidation("001010", mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error.type)
    assertEquals("ERR4 - Does not match agreed input", error.message)
    assertEquals("Current offence", error.fields?.first())
  }

  private fun validOVPRiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    version = "1_0",
    gender = Gender.MALE,
    dateOfBirth = LocalDate.of(1964, 10, 15),
    dateOfCurrentConviction = LocalDate.of(2014, 12, 13),
    dateAtStartOfFollowup = LocalDate.of(2027, 12, 12),
    totalNumberOfSanctions = 10 as Integer?,
    totalNumberOfViolentSanctions = 30 as Integer?,
    impactOfOffendingOnOthers = true,
    currentAccommodation = true,
    ageAtFirstSanction = null,
    currentOffence = null,
    employmentStatus = true,
    alcoholIsCurrentUseAProblem = ProblemLevel.NO_PROBLEMS,
    alcoholExcessive6Months = ProblemLevel.NO_PROBLEMS,
    currentPsychiatricTreatmentOrPending = false,
    temperControl = ProblemLevel.NO_PROBLEMS,
    proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
  )
}
