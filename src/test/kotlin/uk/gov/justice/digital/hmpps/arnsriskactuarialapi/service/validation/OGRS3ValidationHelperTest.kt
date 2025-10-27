package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate

class OGRS3ValidationHelperTest {

  @Test
  fun `validateOGRS3 validRiskScoreRequest no validation errors`() {
    val errors = validateOGRS3(validOGRS3RiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateOGRS3 totalNumberOfSanctions validation error`() {
    val riskScoreRequest = validOGRS3RiskScoreRequest()
      .copy(totalNumberOfSanctionsForAllOffences = 0 as Integer)
    val errors = validateOGRS3(riskScoreRequest)
    assertEquals(1, errors.size)
    assertEquals(ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_LESS_THAN_ONE, errors[0].type)
    assertEquals("Total number of sanctions must be one or greater", errors[0].message)
    assertEquals(listOf("totalNumberOfSanctionsForAllOffences"), errors[0].fields)
  }

  @Test
  fun `validateOGRS3 currentOffenceCode validation error`() {
    val riskScoreRequest = validOGRS3RiskScoreRequest()
      .copy(currentOffenceCode = "123456")
    val errors = validateOGRS3(riskScoreRequest)
    assertEquals(1, errors.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT, errors[0].type)
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
    val result = validateOGRS3(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateOfCurrentConviction",
      "dateAtStartOfFollowupCalculated",
      "totalNumberOfSanctionsForAllOffences",
      "ageAtFirstSanction",
      "currentOffenceCode",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @ParameterizedTest
  @ValueSource(ints = [10, 11])
  fun `validateAgeAtCurrentConviction when age equal to or greater than minimum conviction age`(inputAge: Int) {
    val errors = mutableListOf<ValidationErrorResponse>()
    validateAgeAtCurrentConviction(inputAge, errors)
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAgeAtCurrentConviction when age is less than minimum conviction age`() {
    val errors = mutableListOf<ValidationErrorResponse>()
    validateAgeAtCurrentConviction(9, errors)
    assertEquals(1, errors.size)
    val validationError = errors[0]
    assertNotNull(validationError)
    assertEquals(ValidationErrorType.AGE_AT_CURRENT_CONVICTION_LESS_THAN_TEN, validationError.type)
    assertEquals("Age at current conviction must be 10 or greater", validationError.message)
    assertEquals(listOf("dateOfBirth", "dateOfCurrentConviction"), validationError.fields)
  }

  @ParameterizedTest
  @ValueSource(ints = [10, 11])
  fun `validateAgeAtFirstSanction when age at first sanction is equal to or less than age  at current conviction`(
    ageAtFirstSanctionInput: Int,
  ) {
    val errors = mutableListOf<ValidationErrorResponse>()
    validateAgeAtFirstSanction(ageAtFirstSanctionInput, 11, errors)
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAgeAtFirstSanction when age at first sanction is greater than age  at current conviction`() {
    val errors = mutableListOf<ValidationErrorResponse>()
    validateAgeAtFirstSanction(20, 1, errors)
    assertEquals(1, errors.size)
    val validationError = errors[0]
    assertNotNull(validationError)
    assertEquals(ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION, validationError.type)
    assertEquals("Age at first sanction must be before age at current conviction", validationError.message)
    assertEquals(listOf("dateOfBirth", "dateOfCurrentConviction", "ageAtFirstSanction"), validationError.fields)
  }

  private fun validOGRS3RiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    RiskScoreVersion.V1_0,
    Gender.MALE,
    FIXED_TEST_DATE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    10 as Integer?,
    30 as Integer?,
    "05110",
    dateAtStartOfFollowupCalculated = LocalDate.of(2027, 12, 12),
  )

  @Test
  fun `validateDisallowedCurrentOffenceCode throws exception for disallowed code '00000'`() {
    val disallowedCurrentOffenceCode = "00000"
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = disallowedCurrentOffenceCode)

    val exception = assertThrows(IllegalArgumentException::class.java) {
      validateDisallowedCurrentOffenceCode(riskScoreRequestInput)
    }

    assertTrue(
      exception.message!! == "Offence code cannot be '$disallowedCurrentOffenceCode'",
      "Exception message should contain offence code '$disallowedCurrentOffenceCode'",
    )
  }
}
