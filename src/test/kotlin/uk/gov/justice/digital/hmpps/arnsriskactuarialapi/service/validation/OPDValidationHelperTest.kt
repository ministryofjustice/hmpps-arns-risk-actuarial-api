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
    val result = validateOPD(validOPDRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `opdInitialValidation missing field error with all field populated`() {
    val request = validOPDRiskScoreRequest().copy(
      gender = null,
      overallRiskForAssessment = null,
      highestRiskLevelOverAllAssessments = null,
      isEligibleForMappa = null,
      currentOffenceCode = null,
      hasCustodialSentence = null,
      evidenceOfDomesticAbuse = false,
    )
    val result = validateOPD(request)

    val expectedFields = listOf(
      "gender",
      "overallRiskForAssessment",
      "highestRiskLevelOverAllAssessments",
      "currentOffenceCode",
      "hasCustodialSentence",
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
    val result = validateOPD(request)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `opdInitialValidation missing isEligibleForMappa FEMALE`() {
    val request = validOPDRiskScoreRequest().copy(
      gender = Gender.FEMALE,
      isEligibleForMappa = null,
    )
    val result = validateOPD(request)

    val expectedFields = listOf(
      "isEligibleForMappa",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation missing evidenceOfDomesticAbuse fields error with all field populated`() {
    val request = validOPDRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = true,
      domesticAbuseAgainstPartner = null,
      domesticAbuseAgainstFamily = null,
    )
    val result = validateOPD(request)

    val expectedFields = listOf(
      "domesticAbuseAgainstPartner",
      "domesticAbuseAgainstFamily",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `opdInitialValidation wrong evidenceOfDomesticAbuse fields when evidenceOfDomesticAbuse not provided`() {
    val request = validOPDRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = false,
      domesticAbuseAgainstPartner = true,
      domesticAbuseAgainstFamily = false,
    )
    val result = validateOPD(request)

    val expectedFields = listOf(
      "domesticAbuseAgainstPartner",
      "domesticAbuseAgainstFamily",
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
    val result = validateOPD(request)

    val error = result.first()
    assertEquals(ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT, error.type)
  }
}
