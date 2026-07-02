package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.directContactSexualReoffendingPredictor.DirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.CombinedSeriousReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest

@ExtendWith(MockitoExtension::class)
class CombinedSeriousReoffendingPredictorRiskProducerServiceTest {

  @Mock
  private lateinit var validator: CombinedSeriousReoffendingPredictorValidator

  @InjectMocks
  private lateinit var service: CombinedSeriousReoffendingPredictorRiskProducerService

  @Test
  fun `should calculate score when no validation errors`() {
    val context = emptyContext().copy(
      directContactSexualReoffendingPredictor = DirectContactSexualReoffendingPredictorObject(
        score = 0.0512312312,
        band = RiskBand.LOW,
        pointScore = null,
        validationError = emptyList(),
        femaleVersion = false,
        sexualOffenceHistory = false,
        riskReduction = null,
        featureValues = null,
      ),
      imagesAndIndirectContactSexualReoffendingPredictor = ImagesAndIndirectContactSexualReoffendingPredictorObject(
        score = 2.12,
        band = RiskBand.MEDIUM,
        femaleVersion = false,
        hasEverCommittedSexualOffence = true,
        staticOrDynamic = StaticOrDynamic.STATIC,
        validationErrors = null,
        featureValues = null,
      ),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        0.0312312312,
        RiskBand.LOW,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )
    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest()
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    val result = service.getRiskScore(request, context)

    val combinedSeriousReoffendingPredictor = result.combinedSeriousReoffendingPredictorObject!!
    assertEquals(RiskBand.LOW, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorBand)
    assertEquals(5.12, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorScore)
    assertEquals(RiskBand.MEDIUM, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(2.12, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(7.27, combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorScore)
    assertNotNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorBand)
    assertEquals(StaticOrDynamic.DYNAMIC, combinedSeriousReoffendingPredictor.scoreType)
    assertTrue(combinedSeriousReoffendingPredictor.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate score when female with female contribution to score no validation errors`() {
    val context = emptyContext().copy(
      directContactSexualReoffendingPredictor = directContactSexualReoffendingPredictorNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        0.1,
        RiskBand.LOW,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest().copy(
      gender = Gender.FEMALE,
      hasEverCommittedSexualOffence = true,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    val result = service.getRiskScore(request, context)

    val combinedSeriousReoffendingPredictor = result.combinedSeriousReoffendingPredictorObject!!
    assertEquals(0.48, combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorScore)
    assertNotNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorBand)
    assertEquals(StaticOrDynamic.DYNAMIC, combinedSeriousReoffendingPredictor.scoreType)
    assertTrue(combinedSeriousReoffendingPredictor.validationError?.isEmpty() == true)
  }

  @Test
  fun `should return null CSRP and null scoreType when blocking errors exist `() {
    val errors = listOf(
      ValidationError(
        type = ValidationErrorType.COMPONENT_VALIDATION_ERROR,
        message = "Validation error(s) in component scores",
        fields = listOf(AlgorithmResponse.DIRECT_CONTACT_SEXUAL_REOFFENDING_PREDICTOR.name),
      ),
    )
    val context = emptyContext().copy(
      directContactSexualReoffendingPredictor = DirectContactSexualReoffendingPredictorObject(
        score = 0.02378462734,
        band = RiskBand.LOW,
        pointScore = null,
        validationError = errors,
        riskReduction = null,
        femaleVersion = false,
        sexualOffenceHistory = true,
        featureValues = null,
      ),
    )

    val request = RiskScoreRequest()
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    val result = service.getRiskScore(request, context)

    val combinedSeriousReoffendingPredictor = result.combinedSeriousReoffendingPredictorObject!!
    assertNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorScore)
    assertNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorBand)
    assertNull(combinedSeriousReoffendingPredictor.scoreType)
    assertEquals(errors, combinedSeriousReoffendingPredictor.validationError)
  }

  @Test
  fun `should handle missing static required fields`() {
    val context = emptyContext()
    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest().copy(hasEverCommittedSexualOffence = null, gender = null)

    val error = ValidationError(
      ValidationErrorType.MISSING_MANDATORY_INPUT,
      "Mandatory input field(s) missing",
      listOf(
        "gender",
        "hasEverCommittedSexualOffence",
      ),
    )

    whenever(validator.validateStatic(request)).thenReturn(listOf(error))

    val result = service.getRiskScore(request, context)

    val combinedSeriousReoffendingPredictor = result.combinedSeriousReoffendingPredictorObject!!
    assertEquals(null, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorScore)
    assertEquals(null, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorBand)
    assertEquals(null, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(null, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(null, combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorScore)
    assertNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorBand)
    assertNull(combinedSeriousReoffendingPredictor.scoreType)
    assertThat(combinedSeriousReoffendingPredictor.validationError).contains(error)
  }

  @Test
  fun `should handle no context provided score`() {
    val context = emptyContext()
    val request = validCombinedSeriousReoffendingPredictorStaticRiskScoreRequest()
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    val result = service.getRiskScore(request, context)

    val combinedSeriousReoffendingPredictor = result.combinedSeriousReoffendingPredictorObject!!
    assertEquals(null, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorScore)
    assertEquals(null, combinedSeriousReoffendingPredictor.directContactSexualReoffendingPredictorBand)
    assertEquals(null, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(null, combinedSeriousReoffendingPredictor.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(null, combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorScore)
    assertNull(combinedSeriousReoffendingPredictor.combinedSeriousReoffendingPredictorBand)
    assertNull(combinedSeriousReoffendingPredictor.scoreType)
    assertTrue(combinedSeriousReoffendingPredictor.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate score when upper score limit exceeded and sanitise`() {
    val context = emptyContext().copy(
      directContactSexualReoffendingPredictor = directContactSexualReoffendingPredictorNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        100.0,
        RiskBand.VERY_HIGH,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = false,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(request, context)

    assertEquals(99.99, result.combinedSeriousReoffendingPredictorObject!!.combinedSeriousReoffendingPredictorScore)
  }

  @Test
  fun `should calculate score when lower score limit exceeded and sanitise`() {
    val context = emptyContext().copy(
      directContactSexualReoffendingPredictor = directContactSexualReoffendingPredictorNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        -0.1,
        RiskBand.LOW,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = false,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(request, context)

    assertEquals(0.0, result.combinedSeriousReoffendingPredictorObject!!.combinedSeriousReoffendingPredictorScore)
  }

  private fun directContactSexualReoffendingPredictorNotApplicable(): DirectContactSexualReoffendingPredictorObject = DirectContactSexualReoffendingPredictorObject(
    score = 0.0,
    band = RiskBand.NOT_APPLICABLE,
    pointScore = null,
    validationError = emptyList(),
    femaleVersion = true,
    sexualOffenceHistory = true,
    riskReduction = null,
    featureValues = null,
  )

  private fun imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(): ImagesAndIndirectContactSexualReoffendingPredictorObject = ImagesAndIndirectContactSexualReoffendingPredictorObject(
    score = 0.0,
    band = RiskBand.NOT_APPLICABLE,
    femaleVersion = false,
    hasEverCommittedSexualOffence = false,
    staticOrDynamic = StaticOrDynamic.STATIC,
    validationErrors = null,
    featureValues = null,
  )
}
