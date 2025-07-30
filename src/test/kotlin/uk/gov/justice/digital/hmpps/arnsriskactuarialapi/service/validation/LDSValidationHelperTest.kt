package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.BAD_READING_DIFFICULTY_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.FULL_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.INELIGIBLE_LDS_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.RiskScoreRequestTestConstants.NULL_REQUEST
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ELIGIBLE_FIELDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ERR_LESS_THAN_THREE_FIELDS
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.addEnoughFieldsPresent
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.LDSValidationHelper.Companion.ldsInitialValidation

class LDSValidationHelperTest {

  @Test
  fun `initial validation no errors`() {
    val result = ldsInitialValidation(FULL_LDS_REQUEST)
    assertTrue(result.isEmpty())
  }

  @Test
  fun `fields not eligible`() {
    val result = ldsInitialValidation(NULL_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = ERR_LESS_THAN_THREE_FIELDS,
          fields = ELIGIBLE_FIELDS,
        ),
      ),
      result,
    )
  }

  @Test
  fun `educational field not present but reading difficulty present`() {
    val result = ldsInitialValidation(BAD_READING_DIFFICULTY_LDS_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf(
            "Education Difficulties Field Not Present But Reading Difficulties Present",
          ),
        ),
      ),
      result,
    )
  }

  @Test
  fun `fields not eligible but some present`() {
    val result = ldsInitialValidation(INELIGIBLE_LDS_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.MISSING_INPUT,
          message = "ERR5 - Field is Null",
          fields = listOf(
            "Education Difficulties Field Not Present But Reading Difficulties Present",
            "Education Difficulties Field Not Present But Numeracy Difficulties Present",
          ),
        ),
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = ERR_LESS_THAN_THREE_FIELDS,
          fields = ELIGIBLE_FIELDS,
        ),
      ),
      result,
    )
  }

  @Test
  fun `fields eligible`() {
    val result = mutableListOf<ValidationErrorResponse>().addEnoughFieldsPresent(FULL_LDS_REQUEST)
    assertEquals(emptyList<ValidationErrorResponse>(), result)
  }

  @Test
  fun `fields not eligible -`() {
    val result = mutableListOf<ValidationErrorResponse>().addEnoughFieldsPresent(NULL_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = ERR_LESS_THAN_THREE_FIELDS,
          fields = ELIGIBLE_FIELDS,
        ),
      ),
      result,
    )
  }

  @Test
  fun `fields not eligible but some present -`() {
    val result = mutableListOf<ValidationErrorResponse>().addEnoughFieldsPresent(INELIGIBLE_LDS_REQUEST)
    assertEquals(
      listOf(
        ValidationErrorResponse(
          type = ValidationErrorType.NOT_APPLICABLE,
          message = ERR_LESS_THAN_THREE_FIELDS,
          fields = ELIGIBLE_FIELDS,
        ),
      ),
      result,
    )
  }
}
