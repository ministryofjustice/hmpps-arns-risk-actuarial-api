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
      addUnexpectedFields(mutableListOf("domesticAbuseAgainstPartner", "domesticAbuseAgainstFamily"), mutableListOf())
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.UNEXPECTED_VALUE,
        "ERR6 - Field is unexpected",
        listOf("domesticAbuseAgainstPartner", "domesticAbuseAgainstFamily"),
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
