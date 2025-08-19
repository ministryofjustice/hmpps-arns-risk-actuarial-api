package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSNSVStaticRiskScoreRequest

class SNSVValidationHelperTest {

  @Test
  fun `getNullPropertiesFromPropertiesInitialValidation no errors`() {
    val result = snsvInitialValidation(validSNSVStaticRiskScoreRequest())
    assertTrue(result.isEmpty())
  }

  @Test
  fun `snsvstaticInitialValidation missing field error with all fields null`() {
    val request = validSNSVStaticRiskScoreRequest().copy(
      gender = null,
      dateOfBirth = null,
      dateOfCurrentConviction = null,
      currentOffenceCode = null,
      totalNumberOfSanctionsForAllOffences = null,
      ageAtFirstSanction = null,
      supervisionStatus = null,
      dateAtStartOfFollowup = null,
      totalNumberOfViolentSanctions = null,
    )

    val result = snsvInitialValidation(request)

    val expectedFields = listOf(
      "gender",
      "dateOfBirth",
      "dateOfCurrentConviction",
      "currentOffenceCode",
      "totalNumberOfSanctionsForAllOffences",
      "ageAtFirstSanction",
      "supervisionStatus",
      "dateAtStartOfFollowup",
      "totalNumberOfViolentSanctions",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_INPUT, error.type)
    assertEquals("ERR5 - Field is Null", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
