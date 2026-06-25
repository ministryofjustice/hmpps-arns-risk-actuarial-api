package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate

class CommonValidationHelperTest {

  @Test
  fun `getCurrentOffenceCodeValidation no errors`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "00101")
    val validationErrorResponses = mutableListOf<ValidationError>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = null)
    val validationErrorResponses = mutableListOf<ValidationError>()
    validateCurrentOffenceCode(riskScoreRequestInput, validationErrorResponses)
    assertTrue(validationErrorResponses.isEmpty())
  }

  @Test
  fun `getCurrentOffenceCodeValidation char count error`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "001010")
    val validationErrorResponses = mutableListOf<ValidationError>()
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

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should not add error when total number of sanctions for all offences is null`() {
    val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = null)
    val errors = mutableListOf<ValidationError>()

    validateTotalNumberOfSanctionsForAllOffences(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when totalNumberOfSanctionsForAllOffences is null")
  }

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should not add error when total number of sanctions for all offences is within valid range`() {
    val validValues = listOf(1, 500, 999)

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = validValue)
      val errors = mutableListOf<ValidationError>()

      validateTotalNumberOfSanctionsForAllOffences(request, errors)
      assertTrue(
        errors.isEmpty(),
        "Errors list should be empty for valid totalNumberOfSanctionsForAllOffences value: $validValue",
      )
    }
  }

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should add error when total number of sanctions for all offences is outside of valid range`() {
    val invalidValues = listOf(0, 1000)

    invalidValues.forEach { invalidValue ->
      val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = invalidValue)
      val errors = mutableListOf<ValidationError>()
      validateTotalNumberOfSanctionsForAllOffences(request, errors)

      val expectedError = ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
        listOf("totalNumberOfSanctionsForAllOffences"),
      )
      assertEquals(1, errors.size)
      assertEquals(expectedError, errors.first())
    }
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should not add error when total number of violent sanctions is null`() {
    val request = RiskScoreRequest(
      totalNumberOfViolentSanctions = null,
      totalNumberOfSanctionsForAllOffences = 10,
    )
    val errors = mutableListOf<ValidationError>()

    validateTotalNumberOfViolentSanctions(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when totalNumberOfViolentSanctions is null")
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should not add error when total number of sanctions for all offences is null`() {
    val request = RiskScoreRequest(
      totalNumberOfViolentSanctions = 5,
      totalNumberOfSanctionsForAllOffences = null,
    )
    val errors = mutableListOf<ValidationError>()

    validateTotalNumberOfViolentSanctions(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when totalNumberOfSanctionsForAllOffences is null")
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should not add error when total number of violent sanctions is less than or equal to total number of sanctions for all offences, but not negative`() {
    val validValues = listOf(0, 5, 10)
    val totalNumberOfSanctionsForAllOffences = 10

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(
        totalNumberOfViolentSanctions = validValue,
        totalNumberOfSanctionsForAllOffences = totalNumberOfSanctionsForAllOffences,
      )
      val errors = mutableListOf<ValidationError>()

      validateTotalNumberOfViolentSanctions(request, errors)
      assertTrue(
        errors.isEmpty(),
        "Errors list should be empty for valid totalNumberOfSanctionsForAllOffences value: $validValue (totalNumberOfSanctionsForAllOffences = $totalNumberOfSanctionsForAllOffences)",
      )
    }
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should add error when total number of violent sanctions is negative or greater than total number of sanctions for all offences`() {
    val invalidValues = listOf(-1, 6)

    invalidValues.forEach { invalidValue ->
      val request = RiskScoreRequest(
        totalNumberOfViolentSanctions = invalidValue,
        totalNumberOfSanctionsForAllOffences = 5,
      )
      val errors = mutableListOf<ValidationError>()

      validateTotalNumberOfViolentSanctions(request, errors)
      val expectedError = ValidationErrorType.VIOLENT_SANCTION_OUT_OF_RANGE.asError(
        listOf("totalNumberOfViolentSanctions", "totalNumberOfSanctionsForAllOffences"),
      )
      assertEquals(1, errors.size)
      assertEquals(expectedError, errors.first())
    }
  }

  @Test
  fun `validateAgeAtFirstSanction should not add error when age at first sanction is null`() {
    val request = RiskScoreRequest(ageAtFirstSanction = null)
    val errors = mutableListOf<ValidationError>()

    validateAgeAtFirstSanction(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when ageAtFirstSanction is null")
  }

  @Test
  fun `validateAgeAtFirstSanction should not add error when age at first sanction is within valid range`() {
    val validValues = listOf(8, 45, 98)

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(ageAtFirstSanction = validValue)
      val errors = mutableListOf<ValidationError>()

      validateAgeAtFirstSanction(request, errors)
      assertTrue(errors.isEmpty(), "Errors list should be empty for valid value: $validValue")
    }
  }

  @Test
  fun `validateAgeAtFirstSanction should add error when age at first sanction is outside of valid range`() {
    val invalidValues = listOf(7, 99)

    invalidValues.forEach { invalidValue ->
      val request = RiskScoreRequest(ageAtFirstSanction = invalidValue)
      val errors = mutableListOf<ValidationError>()

      validateAgeAtFirstSanction(request, errors)
      val expectedError = ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(
        listOf("ageAtFirstSanction"),
      )
      assertEquals(1, errors.size)
      assertEquals(expectedError, errors.first())
    }
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstDateOfBirth should not add error when date of current conviction is null`() {
    val request = RiskScoreRequest(
      dateOfCurrentConviction = null,
      dateOfBirth = LocalDate.of(1995, 1, 1),
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when dateOfCurrentConviction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstDateOfBirth should not add error when date of birth is null`() {
    val request = RiskScoreRequest(
      dateOfCurrentConviction = LocalDate.of(2026, 1, 1),
      dateOfBirth = null,
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
    assertTrue(errors.isEmpty(), "Errors list should remain empty when dateOfBirth is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstDateOfBirth should not add error when date of current conviction is after date of birth`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(1995, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2026, 1, 1),
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
    assertTrue(errors.isEmpty(), "Valid chronological dates should not trigger an error")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstDateOfBirth should add error when date of current conviction is exactly equal to date of birth`() {
    val identicalDate = LocalDate.of(2000, 1, 1)
    val request = RiskScoreRequest(
      dateOfBirth = identicalDate,
      dateOfCurrentConviction = identicalDate,
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
    val expectedError = ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(
      listOf("dateOfCurrentConviction"),
    )
    assertEquals(1, errors.size)
    assertEquals(expectedError, errors.first())
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstDateOfBirth should add error when date of current conviction is before date of birth`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2026, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2025, 1, 1),
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstDateOfBirth(request, errors)
    val expectedError = ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(
      listOf("dateOfCurrentConviction"),
    )
    assertEquals(1, errors.size)
    assertEquals(expectedError, errors.first())
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when date of current conviction is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      ageAtFirstSanction = 18,
      dateOfCurrentConviction = null,
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
    assertTrue(errors.isEmpty(), "Validation should skip when dateOfCurrentConviction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when age at first sanction is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      ageAtFirstSanction = null,
      dateOfCurrentConviction = LocalDate.of(2018, 1, 1),
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
    assertTrue(errors.isEmpty(), "Validation should skip when ageAtFirstSanction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when date of birth is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = null,
      ageAtFirstSanction = 18,
      dateOfCurrentConviction = LocalDate.of(2018, 1, 1),
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
    assertTrue(errors.isEmpty(), "Validation should skip when dateOfBirth is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when age at current conviction is greater than or equal to age at first sanction`() {
    val validValues = listOf(LocalDate.of(2018, 1, 1), LocalDate.of(2020, 1, 1))

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(
        dateOfBirth = LocalDate.of(2000, 1, 1),
        dateOfCurrentConviction = validValue,
        ageAtFirstSanction = 18,
      )
      val errors = mutableListOf<ValidationError>()

      validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
      assertTrue(
        errors.isEmpty(),
        "Errors list should remain empty when age at current conviction is greater than or equal to age at first sanction",
      )
    }
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should add error when age at current conviction is less than age at first sanction`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2015, 1, 1),
      ageAtFirstSanction = 18,
    )
    val errors = mutableListOf<ValidationError>()

    validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request, errors)
    val expectedError = ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asError(
      listOf("dateOfCurrentConviction", "ageAtFirstSanction"),
    )
    assertEquals(1, errors.size)
    assertEquals(expectedError, errors.first())
  }
}
