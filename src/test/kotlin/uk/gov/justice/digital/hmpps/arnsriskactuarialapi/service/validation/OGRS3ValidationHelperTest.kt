package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
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
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, errors[0].type)
    assertEquals("ERR4 - Does not match agreed input", errors[0].message)
    assertEquals(listOf("currentOffenceCode"), errors[0].fields)
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
      "dateAtStartOfFollowup",
      "totalNumberOfSanctionsForAllOffences",
      "ageAtFirstSanction",
      "currentOffenceCode",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  private fun validOGRS3RiskScoreRequest(): RiskScoreRequest = RiskScoreRequest(
    RiskScoreVersion.V1_0,
    Gender.MALE,
    FIXED_TEST_DATE,
    LocalDate.of(1964, 10, 15),
    LocalDate.of(2014, 12, 13),
    LocalDate.of(2027, 12, 12),
    10 as Integer?,
    30 as Integer?,
    "05110",
  )
}
