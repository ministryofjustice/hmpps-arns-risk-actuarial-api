package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.ALT_NULL_OGP_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_OGP_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_01569
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_39
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class CommonValidationHelperTest {

  @Test
  fun `no errors present`() {
    val errorResponses = getMissingFieldsValidation(FULL_OGP_REQUEST, TEST_OGP_PROPERTIES_TO_ERRORS)
    assertTrue { errorResponses.isEmpty() }
  }

  @Test
  fun `all test fields absent`() {
    val errorResponses = getMissingFieldsValidation(NULL_REQUEST, TEST_OGP_PROPERTIES_TO_ERRORS)
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.MISSING_INPUT,
        "ERR5 - Field is Null",
        (0..9).map { i -> "$i" },
      ),
    )
    assertEquals(expected, errorResponses)
  }

  @Test
  fun `addMissingCriteriaValidation no errors present`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf(), mutableListOf())
    assertTrue { errorResponses.isEmpty() }
  }

  @Test
  fun `addMissingCriteriaValidation all criteria fields`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf("Gender", "Date of birth"), mutableListOf())
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.NOT_APPLICABLE,
        "ERR - Does not meet eligibility criteria",
        listOf("Gender", "Date of birth"),
      ),
    )
    assertEquals(expected, errorResponses)
  }

  @ParameterizedTest()
  @MethodSource("getRiskScoreRequests")
  fun `error responses are found correctly`(request: RiskScoreRequest, expectedFields: List<String>) {
    val errorResponses = getMissingFieldsValidation(request, TEST_OGP_PROPERTIES_TO_ERRORS)
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.MISSING_INPUT,
        "ERR5 - Field is Null",
        expectedFields,
      ),
    )
    assertEquals(expected, errorResponses)
  }

  companion object {

    val TEST_OGP_PROPERTIES_TO_ERRORS = mapOf(
      "ogrs3TwoYear" to "0",
      "currentAccommodation" to "1",
      "employmentStatus" to "2",
      "regularOffendingActivities" to "3",
      "currentDrugMisuse" to "4",
      "motivationDrug" to "5",
      "problemSolvingSkills" to "6",
      "awarenessOfConsequences" to "7",
      "understandsPeoplesViews" to "8",
      "proCriminalAttitudes" to "9",
    )

    @JvmStatic
    fun getRiskScoreRequests(): List<Arguments> = listOf(
      Arguments.of(
        ALT_NULL_OGP_REQUEST,
        (1..5).map { i -> "${2 * i - 1}" },
      ),
      Arguments.of(
        OGP_REQUEST_39,
        listOf("3", "9"),
      ),
      Arguments.of(
        OGP_REQUEST_01569,
        listOf("0", "1", "5", "6", "9"),
      ),
    )
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
        message = "Unable to produce OGRS3 score due to missing field(s)",
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

  @Test
  fun `validateAge should return no errors with valid parameters`() {
    val result = validateAge(10, 10, mutableListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `validateAge should return 1 error for age under 10`() {
    val result = validateAge(9, 10, mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("Age at current conviction", error.fields?.first())
  }

  @Test
  fun `validateAge should return 1 error for age below first sanction age`() {
    val result = validateAge(11, 15, mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("Age at current conviction", error.fields?.first())
    assertEquals("Age at first sanction", error.fields?.last())
  }

  @Test
  fun `validateAge should not alter exiting errors list if no new errors`() {
    val existingErrors = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("Current offence"),
      ),
    )
    val result = validateAge(22, 15, existingErrors)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.NO_MATCHING_INPUT == result.first().type)
  }

  @Test
  fun `validateAge should add errors to existing list of errors`() {
    val existingErrors = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("Current offence"),
      ),
    )
    val result = validateAge(11, 15, existingErrors)
    assertEquals(2, result.size)
  }
}
