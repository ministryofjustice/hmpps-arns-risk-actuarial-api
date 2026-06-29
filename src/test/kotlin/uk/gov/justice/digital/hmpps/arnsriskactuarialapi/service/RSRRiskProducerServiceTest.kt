package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api.AlgorithmResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor.ImagesAndIndirectContactSexualReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext

class RSRRiskProducerServiceTest {

  private val service = RSRRiskProducerService()

  @Test
  fun `should calculate RSR score when no validation errors`() {
    val context = emptyContext().copy(
      OSPDC = OSPDCObject(
        ospdcScore = 0.0512312312,
        ospdcBand = RiskBand.LOW,
        pointScore = null,
        validationError = emptyList(),
        femaleVersion = false,
        sexualOffenceHistory = false,
        ospRiskReduction = null,
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

    val result = service.getRiskScore(
      RiskScoreRequest(
        hasEverCommittedSexualOffence = false,
        isCurrentOffenceSexuallyMotivated = false,
      ),
      context,
    )

    val rsr = result.RSR!!
    assertEquals(RiskBand.LOW, rsr.ospdcBand)
    assertEquals(5.12, rsr.ospdcScore)
    assertEquals(RiskBand.MEDIUM, rsr.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(2.12, rsr.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(7.27, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertEquals(StaticOrDynamic.DYNAMIC, rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should calculate RSR score when female with female contribution to rsr no validation errors`() {
    val context = emptyContext().copy(
      OSPDC = ospdcNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        0.1,
        RiskBand.LOW,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val result = service.getRiskScore(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
        isCurrentOffenceSexuallyMotivated = false,
      ),
      context,
    )

    val rsr = result.RSR!!
    assertEquals(0.48, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertEquals(StaticOrDynamic.DYNAMIC, rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should return null RSR and null scoreType when blocking errors exist `() {
    val errors = listOf(
      ValidationError(
        type = ValidationErrorType.COMPONENT_VALIDATION_ERROR,
        message = "Validation error(s) in component scores",
        fields = listOf(AlgorithmResponse.OSPDC.name),
      ),
    )
    val context = emptyContext().copy(
      OSPDC = OSPDCObject(
        ospdcScore = 0.02378462734,
        ospdcBand = RiskBand.LOW,
        pointScore = null,
        validationError = errors,
        ospRiskReduction = null,
        femaleVersion = false,
        sexualOffenceHistory = true,
        featureValues = null,
      ),
    )

    val result = service.getRiskScore(RiskScoreRequest(), context)

    val rsr = result.RSR!!
    assertNull(rsr.rsrScore)
    assertNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertEquals(errors, rsr.validationError)
  }

  @Test
  fun `should handle null OSPDC and OSPIIC inputs gracefully`() {
    val context = emptyContext()

    val result = service.getRiskScore(
      RiskScoreRequest(
        hasEverCommittedSexualOffence = false,
        isCurrentOffenceSexuallyMotivated = false,
      ),
      context,
    )

    val rsr = result.RSR!!
    assertEquals(0.0, rsr.ospdcScore)
    assertEquals(RiskBand.NOT_APPLICABLE, rsr.ospdcBand)
    assertEquals(0.0, rsr.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(RiskBand.NOT_APPLICABLE, rsr.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(0.0, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should handle sexualSectionAllNull`() {
    val context = emptyContext()

    val result = service.getRiskScore(RiskScoreRequest(), context)

    val rsr = result.RSR!!
    assertEquals(null, rsr.ospdcScore)
    assertEquals(null, rsr.ospdcBand)
    assertEquals(null, rsr.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(null, rsr.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(null, rsr.rsrScore)
    assertNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should handle sexualOffenceHistoryTrueButSanctionsZero`() {
    val context = emptyContext()

    val result = service.getRiskScore(
      RiskScoreRequest(
        hasEverCommittedSexualOffence = true,
        isCurrentOffenceSexuallyMotivated = true,
        totalContactAdultSexualSanctions = 0,
        totalContactChildSexualSanctions = 0,
        totalNonContactSexualOffences = 0,
        totalIndecentImageSanctions = 0,
      ),
      context,
    )

    val rsr = result.RSR!!
    assertEquals(null, rsr.ospdcScore)
    assertEquals(null, rsr.ospdcBand)
    assertEquals(null, rsr.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(null, rsr.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(null, rsr.rsrScore)
    assertNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `should handle sexualOffenceHistoryFalseButSanctionsNull`() {
    val context = emptyContext()

    val result = service.getRiskScore(RiskScoreRequest(hasEverCommittedSexualOffence = false), context)

    val rsr = result.RSR!!
    assertEquals(0.0, rsr.ospdcScore)
    assertEquals(null, rsr.ospdcBand)
    assertEquals(0.0, rsr.imagesAndIndirectContactSexualReoffendingPredictorScore)
    assertEquals(null, rsr.imagesAndIndirectContactSexualReoffendingPredictorBand)
    assertEquals(0.0, rsr.rsrScore)
    assertNotNull(rsr.rsrBand)
    assertNull(rsr.scoreType)
    assertTrue(rsr.validationError?.isEmpty() == true)
  }

  @Test
  fun `hasBlockingErrors should detect errors that block RSR score`() {
    val service = RSRRiskProducerService()

    val blockingError = listOf(
      ValidationError(
        type = ValidationErrorType.MISSING_MANDATORY_INPUT,
        message = "THIS DOES NOT GET ASSERTED",
        fields = listOf(RiskScoreRequest::isCurrentOffenceSexuallyMotivated.name),
      ),
    )
    val nonBlockingError = listOf(
      ValidationError(
        type = ValidationErrorType.MISSING_MANDATORY_INPUT,
        message = "THIS DOES NOT GET ASSERTED",
        fields = listOf(RiskScoreRequest::hasEverCommittedSexualOffence.name),
      ),
    )
    assertTrue(service.hasBlockingErrors(blockingError))
    assertFalse(service.hasBlockingErrors(nonBlockingError))
  }

  @Test
  fun `should calculate RSR score when upper score limit exceeded and sanitise`() {
    val context = emptyContext().copy(
      OSPDC = ospdcNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        100.0,
        RiskBand.VERY_HIGH,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val result = service.getRiskScore(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        isCurrentOffenceSexuallyMotivated = false,
      ),
      context,
    )

    assertEquals(99.99, result.RSR!!.rsrScore)
  }

  @Test
  fun `should calculate RSR score when lower score limit exceeded and sanitise`() {
    val context = emptyContext().copy(
      OSPDC = ospdcNotApplicable(),
      imagesAndIndirectContactSexualReoffendingPredictor = imagesAndIndirectContactSexualReoffendingPredictorNotApplicable(),
      seriousViolentReoffendingPredictor = SeriousViolentReoffendingPredictorObject(
        -0.1,
        RiskBand.LOW,
        StaticOrDynamic.DYNAMIC,
        emptyList(),
        null,
      ),
    )

    val result = service.getRiskScore(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        isCurrentOffenceSexuallyMotivated = false,
      ),
      context,
    )

    assertEquals(0.0, result.RSR!!.rsrScore)
  }

  private fun ospdcNotApplicable(): OSPDCObject = OSPDCObject(
    ospdcScore = 0.0,
    ospdcBand = RiskBand.NOT_APPLICABLE,
    pointScore = null,
    validationError = emptyList(),
    femaleVersion = true,
    sexualOffenceHistory = true,
    ospRiskReduction = null,
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
