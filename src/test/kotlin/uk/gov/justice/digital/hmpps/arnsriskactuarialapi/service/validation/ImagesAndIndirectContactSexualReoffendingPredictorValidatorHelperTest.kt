package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validImageAndIndirectContactPredictorStaticRiskScoreRequest

class ImagesAndIndirectContactSexualReoffendingPredictorValidatorHelperTest {

  @Test
  fun `validateImagesAndIndirectContactSexualReoffendingPredictor valid static request results in no validation errors`() {
    val errors = validateImagesAndIndirectContactSexualReoffendingPredictor(
      validImageAndIndirectContactPredictorStaticRiskScoreRequest(),
    )
    assertTrue(errors.isEmpty())
  }
// TODO:Validation
//  @Test
//  fun `validateImagesAndIndirectContactSexualReoffendingPredictor missing all mandatory fields error`() {
//    val request = RiskScoreRequest(
//      gender = null,
//      hasEverCommittedSexualOffence = null,
//      totalContactAdultSexualSanctions = null,
//      totalContactChildSexualSanctions = null,
//      totalNonContactSexualOffences = null,
//      totalIndecentImageSanctions = null,
//    )
//    val result = validateImagesAndIndirectContactSexualReoffendingPredictor(request)
//
//    val expectedFields = listOf(
//      "gender",
//      "totalIndecentImageSanctions",
//      "totalContactAdultSexualSanctions",
//      "totalContactChildSexualSanctions",
//      "totalNonContactSexualOffences",
//    )
//
//    val error = result.first()
//    assertEquals(ValidationErrorType.MISSING_MANDATORY_INPUT, error.type)
//    assertEquals("Mandatory input field(s) missing", error.message)
//    assertEquals(expectedFields, error.fields)
//  }
}
