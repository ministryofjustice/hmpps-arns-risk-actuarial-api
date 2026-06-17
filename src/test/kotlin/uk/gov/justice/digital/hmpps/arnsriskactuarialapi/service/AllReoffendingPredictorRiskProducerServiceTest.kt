package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.mockito.Mockito
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAllReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.validateAllReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorDynamicRiskScoreRequest

class AllReoffendingPredictorRiskProducerServiceTest {

  private val service = AllReoffendingPredictorRiskProducerService()

  @Test
  fun `should return early with both static and dynamic errors when static validation fails`() {
    val request = validAllReoffendingPredictorDynamicRiskScoreRequest()

    val staticError = ValidationError(ValidationErrorType.MISSING_MANDATORY_INPUT, "Mandatory input field(s) missing", listOf("ageAtFirstSanction", "gender"))
    val dynamicError = ValidationError(ValidationErrorType.MISSING_DYNAMIC_INPUT, "Dynamic input field(s) missing", listOf("isUnemployed", "hasCannabisUsage"))

    val validationHelperClass = Class.forName("uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.AllReoffendingPredictorValidationHelperKt")

    Mockito.mockStatic(validationHelperClass).use { mockedValidation ->

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorStatic(request)
      }.thenReturn(listOf(staticError))

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorDynamic(request)
      }.thenReturn(listOf(dynamicError))

      val context = service.getRiskScore(request, emptyContext())

      val predictor = context.allReoffendingPredictor
      assertNotNull(predictor)
      assertNull(predictor.score)
      assertNull(predictor.band)
      assertNull(predictor.staticOrDynamic)
      assertEquals(2, predictor.validationErrors?.size)
      assertEquals(staticError, predictor.validationErrors?.get(0))
      assertEquals(dynamicError, predictor.validationErrors?.get(1))
      assertNull(predictor.featureValues)
    }
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val request = validAllReoffendingPredictorDynamicRiskScoreRequest()

    val dynamicError = ValidationError(ValidationErrorType.MISSING_DYNAMIC_INPUT, "Dynamic input field(s) missing", listOf("isUnemployed", "hasCannabisUsage"))

    val validationHelperClass = Class.forName("uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.AllReoffendingPredictorValidationHelperKt")

    Mockito.mockStatic(validationHelperClass).use { mockedValidation ->

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorStatic(request)
      }.thenReturn(emptyList())

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorDynamic(request)
      }.thenReturn(listOf(dynamicError))

      val context = service.getRiskScore(request, emptyContext())

      val predictor = context.allReoffendingPredictor
      assertNotNull(predictor)
      assertNotNull(predictor.score)
      assertNotNull(predictor.band)
      assertEquals(StaticOrDynamic.STATIC, predictor.staticOrDynamic)
      assertEquals(1, predictor.validationErrors?.size)
      assertEquals(dynamicError, predictor.validationErrors?.get(0))
      assertEquals(12, predictor.featureValues?.size)
      assertNotNull(predictor.featureValues?.get(FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.GENDER_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OFFENCE_GROUP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.FIRST_SANCTION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.SECOND_SANCTION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.SECOND_SANCTION_GAP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.COPAS_SCORE.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.COPAS_SCORE_SQUARED.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.TOTAL_WEIGHT.outputName))
    }
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val request = validAllReoffendingPredictorDynamicRiskScoreRequest()

    val validationHelperClass = Class.forName("uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.AllReoffendingPredictorValidationHelperKt")

    Mockito.mockStatic(validationHelperClass).use { mockedValidation ->

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorStatic(request)
      }.thenReturn(emptyList())

      mockedValidation.`when`<List<ValidationError>> {
        validateAllReoffendingPredictorDynamic(request)
      }.thenReturn(emptyList())

      val context = service.getRiskScore(request, emptyContext())

      val predictor = context.allReoffendingPredictor
      assertNotNull(predictor)
      assertNotNull(predictor.score)
      assertNotNull(predictor.band)
      assertEquals(StaticOrDynamic.DYNAMIC, predictor.staticOrDynamic)
      assertEquals(0, predictor.validationErrors?.size)
      assertEquals(33, predictor.featureValues?.size)
      assertNotNull(predictor.featureValues?.get(FeatureValue.TWO_YEAR_INTERCEPT_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.GENDER_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OFFENCE_GROUP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.FIRST_SANCTION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.SECOND_SANCTION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.TOTAL_NUMBER_OF_SANCTIONS_FOR_ALL_OFFENCES_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.SECOND_SANCTION_GAP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.COPAS_SCORE.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.COPAS_SCORE_SQUARED.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.SUITABLE_ACCOMMODATION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.UNEMPLOYED_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.LIVE_IN_RELATIONSHIP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.RELATIONSHIP_QUALITY_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.MULTIPLICATIVE_RELATIONSHIP_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.DOMESTIC_VIOLENCE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.REGULAR_OFFENDING_ACTIVITIES.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.DRUG_MOTIVATION_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.CHRONIC_DRINKING_PROBLEMS_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.BINGE_DRINKING_PROBLEMS_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.IMPULSIVITY_PROBLEMS_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.PRO_CRIMINAL_ATTITUDES_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.HEROIN_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OTHER_OPIATE_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.CRACK_COCAINE_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.POWDER_COCAINE_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.MISUSED_PRESCRIPTION_DRUG_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.BENZODIAZEPINES_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.CANNABIS_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.STEROID_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.OTHER_DRUG_USAGE_WEIGHT.outputName))
      assertNotNull(predictor.featureValues?.get(FeatureValue.TOTAL_WEIGHT.outputName))
    }
  }
}
