package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest
import java.time.LocalDate

class SNSVValidationHelperTest {

  @Test
  fun `getNullPropertiesFromPropertiesInitialValidation no errors`() {
    val result = validateSNSV(validSNSVStaticRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `snsvStaticInitialValidation missing field error with all fields null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      dateOfCurrentConviction = null,
      currentOffenceCode = null,
      totalNumberOfSanctionsForAllOffences = null,
      ageAtFirstSanction = null,
      supervisionStatus = null,
      dateAtStartOfFollowupUserInput = null,
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
      "dateAtStartOfFollowupUserInput",
      "totalNumberOfViolentSanctions",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `snsvDynamicValidation missing field error with derived domesticViolencePerpetrator question null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = null,
      domesticAbuseAgainstPartner = null,
    )
    val errors = mutableListOf<ValidationErrorResponse>()
    snsvDynamicValidation(request, errors)

    val expectedFields = listOf(
      "didOffenceInvolveCarryingOrUsingWeapon",
      "suitabilityOfAccommodation",
      "isUnemployed",
      "currentRelationshipWithPartner",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "impulsivityProblems",
      "temperControl",
      "proCriminalAttitudes",
      "previousConvictions",
      "evidenceOfDomesticAbuse",
    )

    assertEquals(1, errors.size)
    assertEquals(ValidationErrorType.MISSING_DYNAMIC_INPUT, errors.first().type)
    assertEquals("Dynamic input field(s) missing", errors.first().message)
    assertEquals(expectedFields, errors.first().fields)
  }

  @Test
  fun `snsvDynamicValidation missing field error with derived domesticViolencePerpetrator question true but domesticAbuseAgainstPartner is null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = true,
      domesticAbuseAgainstPartner = null,
    )
    val errors = mutableListOf<ValidationErrorResponse>()
    snsvDynamicValidation(request, errors)

    val expectedFields = listOf(
      "didOffenceInvolveCarryingOrUsingWeapon",
      "suitabilityOfAccommodation",
      "isUnemployed",
      "currentRelationshipWithPartner",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "impulsivityProblems",
      "temperControl",
      "proCriminalAttitudes",
      "previousConvictions",
      "domesticAbuseAgainstPartner",
    )

    assertEquals(1, errors.size)
    assertEquals(ValidationErrorType.MISSING_DYNAMIC_INPUT, errors.first().type)
    assertEquals("Dynamic input field(s) missing", errors.first().message)
    assertEquals(expectedFields, errors.first().fields)
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

  @Test
  fun `validateSNSV with invalid total and violent sanction counts`() {
    val inputRiskScoreRequest = validSNSVStaticRiskScoreRequest()
      .copy(totalNumberOfSanctionsForAllOffences = 1 as Integer, totalNumberOfViolentSanctions = 2 as Integer)
    val result = validateSNSV(inputRiskScoreRequest)
    assertEquals(1, result.size)
    assertEquals(ValidationErrorType.VIOLENT_SANCTION_GREATER_THAN_TOTAL_SANCTIONS, result.first().type)
    assertEquals("Violence count is greater than total sanctions", result.first().message)
    assertEquals(listOf("totalNumberOfSanctionsForAllOffences", "totalNumberOfViolentSanctions"), result.first().fields)
  }

  @Test
  fun `validateSNSV with follow up date before Conviction date`() {
    val inputRiskScoreRequest = validSNSVStaticRiskScoreRequest()
      .copy(
        dateOfCurrentConviction = LocalDate.of(2020, 1, 1),
        dateAtStartOfFollowupUserInput = LocalDate.of(2019, 1, 1),
      )
    val result = validateSNSV(inputRiskScoreRequest)
    assertEquals(1, result.size)
    assertEquals(ValidationErrorType.FOLLOW_UP_DATE_BEFORE_CONVICTION_DATE, result.first().type)
    assertEquals("Offender's date of commencement of community sentence or earliest possible release from custody is before conviction date", result.first().message)
    assertEquals(listOf("dateAtStartOfFollowupUserInput", "dateOfCurrentConviction"), result.first().fields)
  }
}
