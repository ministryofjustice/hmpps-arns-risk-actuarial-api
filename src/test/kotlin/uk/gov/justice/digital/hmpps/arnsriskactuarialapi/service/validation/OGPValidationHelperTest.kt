package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_OGP_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.OGP_REQUEST_39
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ogrs3.OGRS3Object
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OGPRiskProducerService.Companion.ogpInitialValidation

class OGPValidationHelperTest {

  companion object {
    val TEST_OSGR3_OBJECT = OGRS3Object(
      ogrs3OneYear = null,
      ogrs3TwoYear = 50,
      band = null,
      validationError = null,
    )
    val CONTEXT_WITH_OSGR3_TWO_YEAR = RiskScoreContext(version = RiskScoreVersion.V1_0, OGRS3 = TEST_OSGR3_OBJECT)
    val CONTEXT_WITH_NO_OSGR3_TWO_YEAR = RiskScoreContext(version = RiskScoreVersion.V1_0, OGRS3 = TEST_OSGR3_OBJECT.copy(ogrs3TwoYear = null))
  }

  @Test
  fun `initial validation no errors`() {
    val result = ogpInitialValidation(FULL_OGP_REQUEST, CONTEXT_WITH_OSGR3_TWO_YEAR)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `initial validation with no OSGR3 Two Year present`() {
    val result = ogpInitialValidation(FULL_OGP_REQUEST, CONTEXT_WITH_NO_OSGR3_TWO_YEAR)
    assertEquals(
      result,
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf("ogrs3TwoYear"),
        ),
      ),
    )
  }

  @Test
  fun `initial validation with OGP_REQUEST_39 and OSGR3 Two Year present`() {
    val result = ogpInitialValidation(OGP_REQUEST_39, CONTEXT_WITH_OSGR3_TWO_YEAR)
    assertEquals(
      result,
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf("regularOffendingActivities", "proCriminalAttitudes"),
        ),
      ),
    )
  }

  @Test
  fun `initial validation with OGP_REQUEST_39 and no OSGR3 Two Year present`() {
    val result = ogpInitialValidation(OGP_REQUEST_39, CONTEXT_WITH_NO_OSGR3_TWO_YEAR)
    assertEquals(
      result,
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf("regularOffendingActivities", "proCriminalAttitudes", "ogrs3TwoYear"),
        ),
      ),
    )
  }
}
