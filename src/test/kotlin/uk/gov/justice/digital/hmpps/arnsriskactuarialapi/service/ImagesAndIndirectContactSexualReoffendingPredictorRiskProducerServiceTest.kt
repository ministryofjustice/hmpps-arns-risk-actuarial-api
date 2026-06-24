package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validImageAndIndirectContactPredictorStaticRiskScoreRequest
import java.math.BigDecimal

class ImagesAndIndirectContactSexualReoffendingPredictorRiskProducerServiceTest {

  private val service = ImagesAndIndirectContactSexualReoffendingPredictorRiskProducerService()

  @Test
  fun `should return early with errors when static validation fails`() {
    val context = service.getRiskScore(RiskScoreRequest(), emptyContext())

    val expectedStaticValidationErrors = ValidationError(
      ValidationErrorType.MISSING_MANDATORY_INPUT,
      "Mandatory input field(s) missing",
      listOf(
        "gender",
        "totalIndecentImageSanctions",
        "totalContactAdultSexualSanctions",
        "totalContactChildSexualSanctions",
        "totalNonContactSexualOffences",
      ),
    )

    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationErrors),
      featureValues = null,
    )

    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }

  @Test
  fun `should return empty object when hasEverCommittedSexualOffence is false`() {
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = false,
      totalContactAdultSexualSanctions = 1,
      totalContactChildSexualSanctions = 1,
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = 1,
    )

    val context = service.getRiskScore(request, emptyContext())

    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = 0.0,
      band = RiskBand.NOT_APPLICABLE,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = null,
      featureValues = null,
    )

    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }

  @Test
  fun `should calculate predictor when static validation pass`() {
    val context = service.getRiskScore(validImageAndIndirectContactPredictorStaticRiskScoreRequest(), emptyContext())

    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = 3.33,
      band = RiskBand.MEDIUM,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = emptyList(),
      featureValues = mapOf(
        "imageAndIndirectContactWeight" to BigDecimal("0.033279999999999997084554337334338924847543239593505859375"),
      ),
    )

    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }
}
