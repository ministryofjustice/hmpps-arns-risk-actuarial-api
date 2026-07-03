package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ImagesAndIndirectContactSexualReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validImageAndIndirectContactPredictorStaticRiskScoreRequest
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class ImagesAndIndirectContactSexualReoffendingPredictorProducerServiceTest {

  @Mock
  private lateinit var validator: ImagesAndIndirectContactSexualReoffendingPredictorValidator

  @InjectMocks
  private lateinit var service: ImagesAndIndirectContactSexualReoffendingPredictorProducerService

  @ParameterizedTest
  @MethodSource("riskScoreRequestProvider")
  fun `should return early with errors when validation fails`(
    riskScoreRequest: RiskScoreRequest,
    expectedStaticValidationErrors: ValidationError,
  ) {
    // Assign
    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = null,
      band = null,
      null,
      null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationErrors),
      featureValues = null,
    )

    whenever(validator.validateStatic(riskScoreRequest)).thenReturn(listOf(expectedStaticValidationErrors))

    // Act
    val context = service.getRiskScore(riskScoreRequest, emptyContext())

    // Assert
    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }

  @Test
  fun `should return empty object when hasEverCommittedSexualOffence is false`() {
    // Assign
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = false,
    )

    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = 0.0,
      band = RiskBand.NOT_APPLICABLE,
      femaleVersion = false,
      hasEverCommittedSexualOffence = false,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = null,
      featureValues = null,
    )

    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    // Act
    val context = service.getRiskScore(request, emptyContext())

    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }

  @Test
  fun `should calculate predictor when static validation pass`() {
    // Assign
    val request = validImageAndIndirectContactPredictorStaticRiskScoreRequest()

    val expected = ImagesAndIndirectContactSexualReoffendingPredictorObject(
      score = 3.33,
      band = RiskBand.MEDIUM,
      femaleVersion = false,
      hasEverCommittedSexualOffence = true,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = emptyList(),
      featureValues = mapOf(
        "imagesAndIndirectContactWeight" to BigDecimal("0.033279999999999997084554337334338924847543239593505859375"),
      ),
    )

    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    // Act
    val context = service.getRiskScore(request, emptyContext())

    // Assert
    assertEquals(expected, context.imagesAndIndirectContactSexualReoffendingPredictor)
  }

  companion object {
    @JvmStatic
    fun riskScoreRequestProvider() = listOf(
      Arguments.of(
        RiskScoreRequest(),
        ValidationError(
          ValidationErrorType.MISSING_MANDATORY_INPUT,
          "Mandatory input field(s) missing",
          listOf("gender", "hasEverCommittedSexualOffence"),
        ),
      ),
      Arguments.of(
        RiskScoreRequest(
          gender = Gender.MALE,
          hasEverCommittedSexualOffence = true,
        ),
        ValidationError(
          ValidationErrorType.MISSING_MANDATORY_INPUT,
          "Mandatory input field(s) missing",
          listOf(
            "totalIndecentImageSanctions",
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalNonContactSexualOffences",
          ),
        ),
      ),
      Arguments.of(
        RiskScoreRequest(
          gender = Gender.MALE,
          hasEverCommittedSexualOffence = false,
          totalContactAdultSexualSanctions = 1,
          totalContactChildSexualSanctions = 1,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 1,
        ),
        ValidationError(
          ValidationErrorType.IMAGES_AND_INDIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR_INCONSISTENT_INPUT,
          "hasEverCommittedSexualOffence cannot be null or false when sexual sanctions exist",
          listOf(
            "hasEverCommittedSexualOffence",
            "totalIndecentImageSanctions",
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalNonContactSexualOffences",
          ),
        ),
      ),
    )
  }
}
