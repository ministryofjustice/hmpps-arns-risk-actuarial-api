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
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = false,
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
      hasEverCommittedSexualOffence = true,
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
      null,
      null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
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
      hasEverCommittedSexualOffence = null,
      isCurrentOffenceSexuallyMotivated = null,
    )

    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      femaleVersion = null,
      sexualOffenceHistory = null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_MANDATORY_INPUT,
          message = "Mandatory input field(s) missing",
          fields = listOf(
            "hasEverCommittedSexualOffence",
          ),
        ),
      ),
    )
    assertEquals(expected, output)
  }

  @Test
  fun `sexual offending inconsistent input, fields should not be populated when sexual offence history is false`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = false,
      isCurrentOffenceSexuallyMotivated = true,
      totalContactChildSexualSanctions = 2,
      totalIndecentImageSanctions = 4,
      totalContactAdultSexualSanctions = 5,
      totalNonContactSexualOffences = 6,
    )

    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      femaleVersion = null,
      sexualOffenceHistory = null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.SEXUAL_OFFENDING_INCONSISTENT_INPUT,
          message = "No sexual motivation/offending identified - additional fields should not be provided",
          fields = listOf(
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalNonContactSexualOffences",
            "totalIndecentImageSanctions",
          ),
        ),
      ),
    )
    assertEquals(expected, output)
  }

  @Test
  fun `sexual offending missing count, fields should be populated when sexual offence history is true`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = false,
    )

    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      femaleVersion = null,
      sexualOffenceHistory = null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS,
          message = "Sexual motivation/offending identified - complete sexual offence counts",
          fields = listOf(
            "totalContactAdultSexualSanctions",
            "totalContactChildSexualSanctions",
            "totalNonContactSexualOffences",
            "totalIndecentImageSanctions",
          ),
        ),
      ),
    )
    assertEquals(expected, output)
  }

  @Test
  fun `missing fields when gender is male and hasEverCommittedSexualOffence is true`() {
    val context = RiskScoreContext(
      version = RiskScoreVersion.V1_0,
    )
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      isCurrentOffenceSexuallyMotivated = false,
      totalIndecentImageSanctions = 4,
    )
    val result = service.getRiskScore(request, context)
    val output = result.OSPIIC
    val expected = OSPIICObject(
      null,
      null,
      femaleVersion = null,
      sexualOffenceHistory = null,
      validationError = listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.SEXUAL_OFFENDING_MISSING_COUNTS,
          message = "Sexual motivation/offending identified - complete sexual offence counts",
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
