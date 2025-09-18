package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType

class CommonValidationHelperTest {

  @Test
  fun `addMissingCriteriaValidation no errors present`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf(), mutableListOf())
    assertTrue { errorResponses.isEmpty() }
  }

  @Test
  fun `addMissingCriteriaValidation all criteria fields`() {
    val errorResponses = addMissingCriteriaValidation(mutableListOf("Gender", "dateOfBirth"), mutableListOf())
    val expected = listOf(
      ValidationErrorResponse(
        ValidationErrorType.NOT_APPLICABLE,
        "ERR1 - Does not meet eligibility criteria",
        listOf("Gender", "dateOfBirth"),
      ),
    )
    assertEquals(expected, errorResponses)
  }

  @Test
  fun `getCurrentOffenceCodeValidation no errors`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "00101")
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = null)
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation char count error`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "001010")
    val validationErrorResponses = mutableListOf<ValidationErrorResponse>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertEquals(1, validationErrorResponses.size)
    assertEquals(ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT, validationErrorResponses.first().type)
    assertEquals("Offence code must be a string of 5 digits", validationErrorResponses.first().message)
    assertEquals(listOf("currentOffenceCode"), validationErrorResponses.first().fields)
  }

  @Test
  fun `addIfNull should field name when property is null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = null)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(listOf("hasPeerGroupInfluences"), missingFields)

    missingFields.addIfNull(request, RiskScoreRequest::gender)
    assertEquals(listOf("hasPeerGroupInfluences", "gender"), missingFields)
  }

  @Test
  fun `addIfNull should not add field name when property is not null`() {
    val request = RiskScoreRequest(hasPeerGroupInfluences = true)
    val missingFields = arrayListOf<String>()

    missingFields.addIfNull(request, RiskScoreRequest::hasPeerGroupInfluences)
    assertEquals(emptyList<String>(), missingFields)
  }
}
