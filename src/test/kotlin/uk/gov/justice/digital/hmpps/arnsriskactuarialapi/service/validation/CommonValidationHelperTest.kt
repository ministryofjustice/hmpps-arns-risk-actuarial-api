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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_0458
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_39
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class CommonValidationHelperTest {

  @Test
  fun `no errors present`() {
    val errorResponses = getMissingPropertiesErrorStrings(FULL_OGP_REQUEST, TEST_OGP_PROPERTIES_TO_ERRORS)
    assertTrue { errorResponses.isEmpty() }
  }

  @Test
  fun `all test fields absent`() {
    val errorStrings = getMissingPropertiesErrorStrings(NULL_REQUEST, TEST_OGP_PROPERTIES_TO_ERRORS)
    val expected = listOf(
      "isCurrentlyOfNoFixedAbodeOrTransientAccommodation",
      "isUnemployed",
      "regularOffendingActivities",
      "currentDrugMisuse",
      "motivationToTackleDrugMisuse",
      "problemSolvingSkills",
      "awarenessOfConsequences",
      "understandsOtherPeoplesViews",
      "proCriminalAttitudes",
    )
    assertEquals(expected, errorStrings)
  }

  @Test
  fun `addMissingCriteriaValidation no errors present`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf(), mutableListOf())
    assertTrue { errorResponses.isEmpty() }
  }

  @Test
  fun `addMissingCriteriaValidation all criteria fields`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf("Gender", "dateOfBirth"), mutableListOf())
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.NOT_APPLICABLE,
        "ERR1 - Does not meet eligibility criteria",
        listOf("Gender", "dateOfBirth"),
      ),
    )
    assertEquals(expected, errorResponses)
  }

  @Test
  fun `addUnexpectedFields should add unexpected errors`() {
    val errorResponses =
      addUnexpectedFields(mutableListOf("domesticAbusePartner", "domesticAbuseFamily"), mutableListOf())
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.UNEXPECTED_VALUE,
        "ERR6 - Field is unexpected",
        listOf("domesticAbusePartner", "domesticAbuseFamily"),
      ),
    )
    assertEquals(expected, errorResponses)
  }

  @ParameterizedTest()
  @MethodSource("getRiskScoreRequests")
  fun `error responses are found correctly`(request: RiskScoreRequest, expected: List<String>) {
    val errorStrings = getMissingPropertiesErrorStrings(request, TEST_OGP_PROPERTIES_TO_ERRORS)
    assertEquals(expected, errorStrings)
  }

  companion object {

    val TEST_OGP_PROPERTIES_TO_ERRORS = listOf(
      "isCurrentlyOfNoFixedAbodeOrTransientAccommodation",
      "isUnemployed",
      "regularOffendingActivities",
      "currentDrugMisuse",
      "motivationToTackleDrugMisuse",
      "problemSolvingSkills",
      "awarenessOfConsequences",
      "understandsOtherPeoplesViews",
      "proCriminalAttitudes",
    )

    @JvmStatic
    fun getRiskScoreRequests(): List<Arguments> = listOf(
      Arguments.of(
        ALT_NULL_OGP_REQUEST,
        listOf("isCurrentlyOfNoFixedAbodeOrTransientAccommodation", "regularOffendingActivities", "motivationToTackleDrugMisuse", "awarenessOfConsequences", "proCriminalAttitudes"),
      ),
      Arguments.of(
        OGP_REQUEST_39,
        listOf("regularOffendingActivities", "proCriminalAttitudes"),

      ),
      Arguments.of(
        OGP_REQUEST_0458,
        listOf("isCurrentlyOfNoFixedAbodeOrTransientAccommodation", "motivationToTackleDrugMisuse", "problemSolvingSkills", "proCriminalAttitudes"),

      ),
    )
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no errors`() {
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(1 as Integer?, mutableListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no error added when number of sanctions is null`() {
    val missingFieldError = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("totalNumberOfSanctionsForAllOffences"),
      ),
    )
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.BELOW_MIN_VALUE == result.first().type)
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation below min value error`() {
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(0 as Integer?, mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("totalNumberOfSanctionsForAllOffences", error.fields?.first())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no errors`() {
    val result = getCurrentOffenceCodeValidation("00101", mutableListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val missingFieldError = mutableListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("currentOffenceCode"),
      ),
    )
    val result = getCurrentOffenceCodeValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.NO_MATCHING_INPUT == result.first().type)
  }

  @Test
  fun `getCurrentOffenceCodeValidation char count error`() {
    val result = getCurrentOffenceCodeValidation("001010", mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error.type)
    assertEquals("ERR4 - Does not match agreed input", error.message)
    assertEquals("currentOffenceCode", error.fields?.first())
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
    assertEquals("dateOfBirth", error.fields?.first())
  }

  @Test
  fun `validateAge should return 1 error for age below first sanction age`() {
    val result = validateAge(11, 15, mutableListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("dateOfBirth", error.fields?.first())
    assertEquals("ageAtFirstSanction", error.fields?.last())
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
    val existingErrors = arrayListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("Current offence"),
      ),
    )
    val result = validateAge(11, 15, existingErrors)
    assertEquals(2, result.size)
  }

  @Test
  fun `addIfNull should field name when property is null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = null)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(listOf("hasPeerGroupInfluences"), missingFields)

    missingFields.addIfNull(request, RiskScoreRequest::gender)
    assertEquals(listOf("hasPeerGroupInfluences", "gender"), missingFields)
  }

  @Test
  fun `addIfNull should not add field name when property is not null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = true)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(emptyList<String>(), missingFields)
  }
}
