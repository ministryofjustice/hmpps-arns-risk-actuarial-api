package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOPDRiskScoreRequest
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
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
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
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
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  private fun domesticAbuseInconsistentInputTestInputs(): Stream<Arguments> = Stream.of(
    Arguments.of(null, true, null),
    Arguments.of(null, null, true),
    Arguments.of(null, false, null),
    Arguments.of(null, null, false),
    Arguments.of(false, true, null),
    Arguments.of(false, null, true),
    Arguments.of(false, false, null),
    Arguments.of(false, null, false),
  )

  @ParameterizedTest
  @MethodSource("domesticAbuseInconsistentInputTestInputs")
  fun `opdInitialValidation wrong evidenceOfDomesticAbuse fields when evidenceOfDomesticAbuse not provided`(
    evidenceOfDomesticAbuseInput: Boolean?,
    domesticAbuseAgainstPartnerInput: Boolean?,
    domesticAbuseAgainstFamilyInput: Boolean?,
  ) {
    val request = validOPDRiskScoreRequest().copy(
      evidenceOfDomesticAbuse = evidenceOfDomesticAbuseInput,
      domesticAbuseAgainstPartner = domesticAbuseAgainstPartnerInput,
      domesticAbuseAgainstFamily = domesticAbuseAgainstFamilyInput,
    )
    val result = validateOPD(request)

    val error = result.first()
    assertEquals(ValidationErrorType.DOMESTIC_ABUSE_INCONSISTENT_INPUT, error.type)
    assertEquals("No evidence of domestic abuse identified - domesticAbuseAgainstPartner and domesticAbuseAgainstFamily should not be provided", error.message)
    assertEquals(
      listOf(
        "evidenceOfDomesticAbuse",
        "domesticAbuseAgainstFamily",
        "domesticAbuseAgainstPartner",
      ),
      error.fields,
    )
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
