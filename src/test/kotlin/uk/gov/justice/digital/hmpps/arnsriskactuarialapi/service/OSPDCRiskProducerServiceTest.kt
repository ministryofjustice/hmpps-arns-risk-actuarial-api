package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validOSPDCRiskScoreRequest
import java.time.LocalDate
import kotlin.test.assertFailsWith

class OSPDCRiskProducerServiceTest {

  private val service: OSPDCRiskProducerService = OSPDCRiskProducerService()

  @Test
  fun `should return valid OSPDCObject for valid input LOW risk`() {
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1 as Integer,
          totalContactAdultSexualSanctions = 0,
          totalContactChildSexualSanctions = 0,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.LOW, result.OSPDC!!.ospdcBand)
    assertTrue(result.OSPDC!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input MEDIUM risk`() {
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1 as Integer,
          totalContactAdultSexualSanctions = 1,
          totalContactChildSexualSanctions = 1,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.MEDIUM, result.OSPDC!!.ospdcBand)
    assertTrue(result.OSPDC!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input HIGH risk`() {
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1 as Integer,
          totalContactAdultSexualSanctions = 2,
          totalContactChildSexualSanctions = 2,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.HIGH, result.OSPDC!!.ospdcBand)
    assertTrue(result.OSPDC!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return valid OVPObject for valid input VERY_HIGH risk`() {
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest()
        .copy(
          totalNumberOfSanctionsForAllOffences = 1 as Integer,
          totalContactAdultSexualSanctions = 3,
          totalContactChildSexualSanctions = 3,
          totalNonContactSexualOffences = 1,
          totalIndecentImageSanctions = 0,
        ),
      emptyContext(),
    )

    assertNotNull(result)
    assertEquals(RiskBand.VERY_HIGH, result.OSPDC!!.ospdcBand)
    assertTrue(result.OSPDC!!.validationError.isNullOrEmpty())
  }

  @Test
  fun `should return NOT_APPLICABLE when FEMALE with sexual offences`() {
    // When
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest().copy(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.00383142, result.OSPDC?.ospdcScore)
    assertEquals(RiskBand.NOT_APPLICABLE, result.OSPDC?.ospdcBand)
    assertEquals(1, result.OSPDC?.validationError?.size)
    val error = result.OSPDC?.validationError?.first()
    assertEquals(ValidationErrorType.NOT_APPLICABLE, error?.type)
    assertEquals("ERR1 - Does not meet eligibility criteria", error?.message)
  }

  @Test
  fun `should still return a score when no sexual offences`() {
    // When
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest().copy(
        hasEverCommittedSexualOffence = false,
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.OSPDC?.ospdcScore)
    assertEquals(RiskBand.NOT_APPLICABLE, result.OSPDC?.ospdcBand)
  }

  @Test
  fun `should return NOT_APPLICABLE OSPDCObject when 64 point score is 0`() {
    // When
    val result = service.getRiskScore(
      validOSPDCRiskScoreRequest().copy(
        dateOfBirth = LocalDate.of(1950, 1, 1),
        totalContactAdultSexualSanctions = 0,
        totalContactChildSexualSanctions = 0,
        totalNonContactSexualOffences = 0,
        totalIndecentImageSanctions = 0,
        dateOfMostRecentSexualOffence = LocalDate.of(1963, 1, 1),
        totalNumberOfSanctionsForAllOffences = 1 as Integer,
        isCurrentOffenceAgainstVictimStranger = false,
      ),
      emptyContext(),
    )

    // Then
    assertNotNull(result)
    assertEquals(0.0, result.OSPDC?.ospdcScore)
    assertEquals(RiskBand.LOW, result.OSPDC?.ospdcBand)
    assertEquals(0, result.OSPDC?.validationError?.size)
  }

  @Test
  fun `should throw exception when calculation error`() {
    // When
    val exception = assertFailsWith<IllegalArgumentException>(
      block = {
        service.getRiskScore(
          validOSPDCRiskScoreRequest().copy(totalNonContactSexualOffences = -1),
          emptyContext(),
        )
      },
    )

    // Then
    assertEquals("Invalid total non-contact sexual offences excluding indecent images value: -1", exception.message)
  }
}
