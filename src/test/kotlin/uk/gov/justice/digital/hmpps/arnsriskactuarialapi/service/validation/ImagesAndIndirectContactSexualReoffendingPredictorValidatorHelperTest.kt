package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validImageAndIndirectContactPredictorStaticRiskScoreRequest

class ImagesAndIndirectContactSexualReoffendingPredictorValidatorHelperTest {

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor valid static request results in no validation errors`() {
    val errors = validateImagesAndIndirectContactSexualReoffendingPredictor(
      validImageAndIndirectContactPredictorStaticRiskScoreRequest(),
    )
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor missing gender field error`() {
    val request = RiskScoreRequest(
      gender = null,
      hasEverCommittedSexualOffence = null,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
    )
    val result = validateImagesAndIndirectContactSexualReoffendingPredictor(request)

    val expectedFields = listOf("gender", "hasEverCommittedSexualOffence")

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor missing sanctions fields error`() {
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
    )
    val result = validateImagesAndIndirectContactSexualReoffendingPredictor(request)

    val expectedFields = listOf(
      "totalIndecentImageSanctions",
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
    assertEquals("Mandatory input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor zero sanctions fields error`() {
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalNonContactSexualOffences = 0,
      totalIndecentImageSanctions = 0,
    )
    val result = validateImagesAndIndirectContactSexualReoffendingPredictor(request)

    val expectedFields = listOf(
      "totalIndecentImageSanctions",
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_NO_SANCTIONS, error.type)
    assertEquals("If hasEverCommittedSexualOffence is true, at least one sexual sanction/offence must be more than 0", error.message)
    assertEquals(expectedFields, error.fields)
  }

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor ambiguous input error`() {
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = false,
      totalContactAdultSexualSanctions = 1,
      totalContactChildSexualSanctions = 1,
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = 1,
    )
    val result = validateImagesAndIndirectContactSexualReoffendingPredictor(request)

    val expectedFields = listOf(
      "hasEverCommittedSexualOffence",
      "totalIndecentImageSanctions",
      "totalContactAdultSexualSanctions",
      "totalContactChildSexualSanctions",
      "totalNonContactSexualOffences",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.AMBIGUOUS_INPUT, error.type)
    assertEquals("Ambiguous input fields", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
