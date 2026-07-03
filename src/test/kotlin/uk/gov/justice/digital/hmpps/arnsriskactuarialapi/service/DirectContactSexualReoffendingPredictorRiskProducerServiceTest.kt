package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.DirectContactSexualReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validDirectContactSexualReoffendingPredictorRiskScoreRequest
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class DirectContactSexualReoffendingPredictorRiskProducerServiceTest {

  @Mock
  private lateinit var validator: DirectContactSexualReoffendingPredictorValidator

  @InjectMocks
  private lateinit var service: DirectContactSexualReoffendingPredictorProducerService

  @Test
  fun `should calculate predictor for valid input LOW risk`() {
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest()
        .copy(
          gender = Gender.MALE,
          hasEverCommittedSexualOffence = true,
          totalNumberOfSanctionsForAllOffences = 1,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.LOW, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should calculate predictor for valid input MEDIUM risk`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      totalNumberOfSanctionsForAllOffences = 1,
      totalContactAdultSexualSanctions = 1,
      totalContactChildSexualSanctions = 1,
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = 0,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.MEDIUM, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should calculate predictor for valid input HIGH risk`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      totalNumberOfSanctionsForAllOffences = 1,
      totalContactAdultSexualSanctions = 2,
      totalContactChildSexualSanctions = 2,
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = 0,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.HIGH, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should calculate predictor for valid input VERY_HIGH risk`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      totalNumberOfSanctionsForAllOffences = 1,
      totalContactAdultSexualSanctions = 3,
      totalContactChildSexualSanctions = 3,
      totalNonContactSexualOffences = 1,
      totalIndecentImageSanctions = 0,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.VERY_HIGH, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should calculate predictor when FEMALE with sexual offences`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      gender = Gender.FEMALE,
      hasEverCommittedSexualOffence = true,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.directContactSexualReoffendingPredictor?.score)
    assertEquals(RiskBand.NOT_APPLICABLE, result.directContactSexualReoffendingPredictor?.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError!!.isEmpty())
  }

  @Test
  fun `should still return a score when no sexual offences`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      hasEverCommittedSexualOffence = false,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.directContactSexualReoffendingPredictor?.score)
    assertEquals(RiskBand.NOT_APPLICABLE, result.directContactSexualReoffendingPredictor?.band)
  }

  @Test
  fun `should return NOT_APPLICABLE DirectContactSexualReoffendingPredictorObject when 64 point score is 0`() {
    val request = validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
      dateOfBirth = LocalDate.of(1950, 1, 1),
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalNonContactSexualOffences = 0,
      totalIndecentImageSanctions = 1,
      dateOfMostRecentSexualOffence = LocalDate.of(1963, 1, 1),
      totalNumberOfSanctionsForAllOffences = 1,
      isCurrentOffenceAgainstVictimStranger = false,
    )
    whenever(validator.validateStatic(request)).thenReturn(emptyList())

    val result = service.getRiskScore(
      request,
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.directContactSexualReoffendingPredictor?.score)
    assertEquals(RiskBand.LOW, result.directContactSexualReoffendingPredictor?.band)
    assertEquals(0, result.directContactSexualReoffendingPredictor?.validationError?.size)
  }
}
