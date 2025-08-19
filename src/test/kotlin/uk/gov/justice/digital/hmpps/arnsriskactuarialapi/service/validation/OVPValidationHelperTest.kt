package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOVPRiskScoreRequest

class OVPValidationHelperTest {

  @Test
  fun `getMissingFieldsValidation no errors`() {
    val result = getMissingOVPFieldsValidation(validOVPRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getMissingFieldsValidation missing field error with all field populated`() {
    val request = RiskScoreRequest(
      RiskScoreVersion.V1_0,
      null,
      FIXED_TEST_DATE,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    val result = getMissingOVPFieldsValidation(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateAtStartOfFollowup",
      "totalNumberOfSanctionsForAllOffences",
      "totalNumberOfViolentSanctions",
      "doesRecogniseImpactOfOffendingOnOthers",
      "isCurrentlyOfNoFixedAbodeOrTransientAccommodation",
      "isUnemployed",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "hasCurrentPsychiatricTreatment",
      "temperControl",
      "proCriminalAttitudes",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no errors`() {
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(1 as Integer?, arrayListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation no error added when number of sanctions is null`() {
    val missingFieldError = arrayListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OVP score due to missing field(s)",
        fields = listOf("Total number of sanctions"),
      ),
    )
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.BELOW_MIN_VALUE == result.first().type)
  }

  @Test
  fun `getTotalNumberOfSanctionsValidation below min value error`() {
    val result = getTotalNumberOfSanctionsForAllOffencesValidation(0 as Integer?, arrayListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.BELOW_MIN_VALUE, error.type)
    assertEquals("ERR2 - Below minimum value", error.message)
    assertEquals("totalNumberOfSanctionsForAllOffences", error.fields?.first())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no errors`() {
    val result = getCurrentOffenceCodeValidation("00101", arrayListOf())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val missingFieldError = arrayListOf(
      ValidationErrorResponse(
        type = ValidationErrorType.MISSING_INPUT,
        message = "Unable to produce OGRS3 score due to missing field(s)",
        fields = listOf("Current offence"),
      ),
    )
    val result = getCurrentOffenceCodeValidation(null, missingFieldError)
    assertTrue(result.count() == 1)
    assertFalse(ValidationErrorType.NO_MATCHING_INPUT == result.first().type)
  }

  @Test
  fun `getCurrentOffenceValidation char count error`() {
    val result = getCurrentOffenceCodeValidation("001010", arrayListOf())
    val error = result.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error.type)
    assertEquals("ERR4 - Does not match agreed input", error.message)
    assertEquals("currentOffenceCode", error.fields?.first())
  }
}
