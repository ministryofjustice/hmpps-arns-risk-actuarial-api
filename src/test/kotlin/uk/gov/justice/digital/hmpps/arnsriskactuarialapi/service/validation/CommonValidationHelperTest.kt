package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
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
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "00101")
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = null)
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation char count error`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "001010")
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertEquals(1, validationErrorResponses.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT, validationErrorResponses.first().type)
    assertEquals("Offence code must be a string of 5 digits", validationErrorResponses.first().message)
    assertEquals(listOf("currentOffenceCode"), validationErrorResponses.first().fields)
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
