package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OSPIICRiskProducerServiceTest {

  private val service = OSPIICRiskProducerService()

  @Test
  fun `single request`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      totalContactChildSexualSanctions = 2,
      totalIndecentImageSanctions = 4,
      totalContactAdultSexualSanctions = 5,
      totalNonContactSexualOffences = 6,
    )
    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    assertEquals(RiskBand.HIGH, output!!.band)
    assertEquals(0.1031, output.score!!, 0.00001)
    assertEquals(emptyList<ValidationErrorResponse>(), output.validationError)
  }

  @Test
  fun `when gender is female`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.FEMALE,
    )
    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    assertEquals(RiskBand.NOT_APPLICABLE, output!!.band)
    assertEquals(0.0, output.score!!, 0.00001)
    assertEquals(emptyList<ValidationErrorResponse>(), output.validationError)
  }

  @Test
  fun `missing gender`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      totalContactChildSexualSanctions = 2,
      hasEverCommittedSexualOffence = true,
      totalIndecentImageSanctions = 4,
    )
    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf("gender"),
        ),
      ),
    )
    assertEquals(expected, output)
  }

  @Test
  fun `unexpected value, fields not populated when sexual offence history is null`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      totalContactChildSexualSanctions = 2,
      totalIndecentImageSanctions = 4,
      totalContactAdultSexualSanctions = 5,
      totalNonContactSexualOffences = 6,
      hasEverCommittedSexualOffence = false,
    )

    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      RiskBand.NOT_APPLICABLE,
      0.0,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: No sexual motivation/offending identified - fields should not be populated.",
          fields = listOf(
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalIndecentImageSanctions",
            "totalNonContactSexualOffences",
          ),
        ),
      ),
    )
    assertEquals(expected, output)
  }

  @Test
  fun `missing fields when gender is male`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      totalIndecentImageSanctions = 4,
    )
    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf(
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalNonContactSexualOffences",
          ),
        ),
      ),
    )
    assertEquals(expected, output)
  }
}
