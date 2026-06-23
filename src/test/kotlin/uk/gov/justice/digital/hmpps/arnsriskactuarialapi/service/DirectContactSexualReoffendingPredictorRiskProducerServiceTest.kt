package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validDirectContactSexualReoffendingPredictorRiskScoreRequest
import java.time.LocalDate

class DirectContactSexualReoffendingPredictorRiskProducerServiceTest {

  private val service: DirectContactSexualReoffendingPredictorRiskProducerService = DirectContactSexualReoffendingPredictorRiskProducerService()

  @Test
  fun `should return valid DirectContactSexualReoffendingPredictorObject for valid input LOW risk`() {
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
  fun `should return valid OVPObject for valid input MEDIUM risk`() {
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1,
          totalContactAdultSexualSanctions = 1,
          totalContactChildSexualSanctions = 1,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.MEDIUM, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid DirectContactSexualReoffendingPredictorObject for valid input HIGH risk`() {
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1,
          totalContactAdultSexualSanctions = 2,
          totalContactChildSexualSanctions = 2,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.HIGH, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid DirectContactSexualReoffendingPredictorObject for valid input VERY_HIGH risk`() {
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1,
          totalContactAdultSexualSanctions = 3,
          totalContactChildSexualSanctions = 3,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.VERY_HIGH, result.directContactSexualReoffendingPredictor!!.band)
    assertTrue(result.directContactSexualReoffendingPredictor!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return NOT_APPLICABLE DirectContactSexualReoffendingPredictorObject when FEMALE with sexual offences`() {
    // When
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
      ),
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
    // When
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
        hasEverCommittedSexualOffence = false,
        totalContactAdultSexualSanctions = null,
        totalContactChildSexualSanctions = null,
        totalNonContactSexualOffences = null,
        totalIndecentImageSanctions = null,
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.directContactSexualReoffendingPredictor?.score)
    assertEquals(RiskBand.NOT_APPLICABLE, result.directContactSexualReoffendingPredictor?.band)
  }

  @Test
  fun `should return NOT_APPLICABLE DirectContactSexualReoffendingPredictorObject when 64 point score is 0`() {
    // When
    val result = service.getRiskScore(
      validDirectContactSexualReoffendingPredictorRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1950, 1, 1),
        totalContactAdultSexualSanctions = 0,
        totalContactChildSexualSanctions = 0,
        totalNonContactSexualOffences = 0,
        totalIndecentImageSanctions = 1,
        dateOfMostRecentSexualOffence = LocalDate.of(1963, 1, 1),
        totalNumberOfSanctionsForAllOffences = 1,
        isCurrentOffenceAgainstVictimStranger = false,
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.directContactSexualReoffendingPredictor?.score)
    assertEquals(RiskBand.LOW, result.directContactSexualReoffendingPredictor?.band)
    assertEquals(0, result.directContactSexualReoffendingPredictor?.validationError?.size)
  }
}
