package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOPDRiskScoreRequest

class OPDValidationHelperTest {

  @Test
  fun `opdInitialValidation no errors`() {
    val result = opdInitialValidation(validOPDRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `opdInitialValidation missing field error with all field populated`() {
    val request = validOPDRiskScoreRequest().copy(
      gender = null,
      overallRiskForAssessment = null,
      highestRiskLevel = null,
      isEligibleForMappa = null,
      currentOffenceCode = null,
      custodialSentence = null,
      domesticAbuse = false,
    )
    val result = opdInitialValidation(request)

    val expectedFields = listOf(
      "gender",
      "overallRiskForAssessment",
      "highestRiskLevel",
      "currentOffenceCode",
      "custodialSentence",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation missing isEligibleForMappa MALE`() {
    val request = validOPDRiskScoreRequest().copy(
      gender = Gender.MALE,
      isEligibleForMappa = null,
    )
    val result = opdInitialValidation(request)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `opdInitialValidation missing isEligibleForMappa FEMALE`() {
    val request = validOPDRiskScoreRequest().copy(
      gender = Gender.FEMALE,
      isEligibleForMappa = null,
    )
    val result = opdInitialValidation(request)

    val expectedFields = listOf(
      "isEligibleForMappa",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation missing domesticAbuse fields error with all field populated`() {
    val request = validOPDRiskScoreRequest().copy(
      domesticAbuse = true,
      domesticAbusePartner = null,
      domesticAbuseFamily = null,
    )
    val result = opdInitialValidation(request)

    val expectedFields = listOf(
      "domesticAbusePartner",
      "domesticAbuseFamily",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation wrong domesticAbuse fields when domesticAbuse not provided`() {
    val request = validOPDRiskScoreRequest().copy(
      domesticAbuse = false,
      domesticAbusePartner = true,
      domesticAbuseFamily = false,
    )
    val result = opdInitialValidation(request)

    val expectedFields = listOf(
      "domesticAbusePartner",
      "domesticAbuseFamily",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.UNEXPECTED_VALUE, error.type)
    assertEquals("ERR6 - Field is unexpected", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation with invalid currentOffenceCode`() {
    val request = validOPDRiskScoreRequest().copy(
      currentOffenceCode = "BLA",
    )
    val result = opdInitialValidation(request)

    val expectedFields = listOf(
      "currentOffenceCode",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.NO_MATCHING_INPUT, error.type)
    assertEquals("ERR4 - Does not match agreed input", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
