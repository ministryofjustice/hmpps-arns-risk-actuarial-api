package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest

class SNSVValidationHelperTest {

  @Test
  fun `getNullPropertiesFromPropertiesInitialValidation no errors`() {
    val result = validateSNSV(validSNSVStaticRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `snsvstaticInitialValidation missing field error with all fields null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      dateOfCurrentConviction = null,
      currentOffenceCode = null,
      totalNumberOfSanctionsForAllOffences = null,
      ageAtFirstSanction = null,
      supervisionStatus = null,
      dateAtStartOfFollowup = null,
      totalNumberOfViolentSanctions = null,
    )

    val result = validateSNSV(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateOfCurrentConviction",
      "currentOffenceCode",
      "totalNumberOfSanctionsForAllOffences",
      "ageAtFirstSanction",
      "supervisionStatus",
      "dateAtStartOfFollowup",
      "totalNumberOfViolentSanctions",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `snsvDynamicValidation missing field error with derived domesticViolencePerpetrator question null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = null,
      domesticAbuseAgainstPartner = null,
    )

    val result = snsvDynamicValidation(request)

    val expectedFields = listOf(
      "evidenceOfDomesticAbuse",
      "domesticAbuseAgainstPartner",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateSNSV with invalid currentOffenceCode`() {
    val inputRiskScoreRequest = validSNSVStaticRiskScoreRequest()
      .copy(currentOffenceCode = "123456")
    val result = validateSNSV(inputRiskScoreRequest)
    assertEquals(1, result.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT, result.first().type)
    assertEquals("Offence code must be a string of 5 digits", result.first().message)
    assertEquals(listOf("currentOffenceCode"), result.first().fields)
  }
}
