package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommonValidatorTest {

  private val commonValidator = CommonValidator()

  @Test
  fun `getCurrentOffenceCodeValidation no errors`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "00101")
    val validationErrorResponse = commonValidator.validateCurrentOffenceCode(riskScoreRequestInput)
    assertNull(validationErrorResponse)
  }

  @Test
  fun `getCurrentOffenceCodeValidation no error added when current offence null`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = null)
    val validationErrorResponse = commonValidator.validateCurrentOffenceCode(riskScoreRequestInput)
    assertNull(validationErrorResponse)
  }

  @Test
  fun `getCurrentOffenceCodeValidation char count error`() {
    val riskScoreRequestInput = RiskScoreRequest(currentOffenceCode = "001010")
    val validationErrorResponse = commonValidator.validateCurrentOffenceCode(riskScoreRequestInput)
    assertEquals(
      ValidationError(
        type = ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT,
        message = "Offence code must be a string of 5 digits",
        fields = listOf("currentOffenceCode"),
      ),
      validationErrorResponse,
    )
  }

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should not add error when total number of sanctions for all offences is null`() {
    val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = null)
    val error = commonValidator.validateTotalNumberOfSanctionsForAllOffences(request)
    assertNull(error, "Errors list should remain empty when totalNumberOfSanctionsForAllOffences is null")
  }

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should not add error when total number of sanctions for all offences is within valid range`() {
    val validValues = listOf(1, 500, 999)

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = validValue)
      val error = commonValidator.validateTotalNumberOfSanctionsForAllOffences(request)
      assertNull(
        error,
        "Errors list should be empty for valid totalNumberOfSanctionsForAllOffences value: $validValue",
      )
    }
  }

  @Test
  fun `validateTotalNumberOfSanctionsForAllOffences should add error when total number of sanctions for all offences is outside of valid range`() {
    val invalidValues = listOf(0, 1000)

    invalidValues.forEach { invalidValue ->
      val request = RiskScoreRequest(totalNumberOfSanctionsForAllOffences = invalidValue)
      val error = commonValidator.validateTotalNumberOfSanctionsForAllOffences(request)

      val expectedError = ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
        listOf("totalNumberOfSanctionsForAllOffences"),
      )
      assertEquals(expectedError, error)
    }
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should not add error when total number of violent sanctions is null`() {
    val request = RiskScoreRequest(
      totalNumberOfViolentSanctions = null,
      totalNumberOfSanctionsForAllOffences = 10,
    )
    val error = commonValidator.validateTotalNumberOfViolentSanctions(request)
    assertNull(error, "Errors list should remain empty when totalNumberOfViolentSanctions is null")
  }

  @Test
  fun `validateTotalNumberOfViolentSanctions should not add error when total number of sanctions for all offences is null`() {
    val request = RiskScoreRequest(
      totalNumberOfViolentSanctions = 5,
      totalNumberOfSanctionsForAllOffences = null,
    )
    val error = commonValidator.validateTotalNumberOfViolentSanctions(request)
    assertNull(error, "Errors list should remain empty when totalNumberOfSanctionsForAllOffences is null")
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
      val error = commonValidator.validateTotalNumberOfViolentSanctions(request)
      assertNull(
        error,
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
      val error = commonValidator.validateTotalNumberOfViolentSanctions(request)
      val expectedError = ValidationErrorType.VIOLENT_SANCTION_OUT_OF_RANGE.asError(
        listOf("totalNumberOfViolentSanctions", "totalNumberOfSanctionsForAllOffences"),
      )
      assertEquals(expectedError, error)
    }
  }

  @Test
  fun `validateAgeAtFirstSanction should not add error when age at first sanction is null`() {
    val request = RiskScoreRequest(ageAtFirstSanction = null)
    val error = commonValidator.validateAgeAtFirstSanction(request)
    assertNull(error, "Errors list should remain empty when ageAtFirstSanction is null")
  }

  @Test
  fun `validateAgeAtFirstSanction should not add error when age at first sanction is within valid range`() {
    val validValues = listOf(8, 45, 98)

    validValues.forEach { validValue ->
      val request = RiskScoreRequest(ageAtFirstSanction = validValue)
      val error = commonValidator.validateAgeAtFirstSanction(request)
      assertNull(error, "Errors list should be empty for valid value: $validValue")
    }
  }

  @Test
  fun `validateAgeAtFirstSanction should add error when age at first sanction is outside of valid range`() {
    val invalidValues = listOf(7, 99)

    invalidValues.forEach { invalidValue ->
      val request = RiskScoreRequest(ageAtFirstSanction = invalidValue)
      val error = commonValidator.validateAgeAtFirstSanction(request)
      val expectedError = ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(
        listOf("ageAtFirstSanction"),
      )
      assertEquals(expectedError, error)
    }
  }

  @ParameterizedTest
  @MethodSource("test validateDateOfCurrentConvictionAgainstDateOfBirth logic data")
  fun `test validateDateOfCurrentConvictionAgainstDateOfBirth logic`(
    request: RiskScoreRequest,
    expectedError: ValidationError?,
  ) {
    val error = commonValidator.validateDateOfCurrentConvictionAgainstDateOfBirth(request)
    assertEquals(expectedError, error)
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when date of current conviction is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      ageAtFirstSanction = 18,
      dateOfCurrentConviction = null,
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    assertNull(error, "Validation should skip when dateOfCurrentConviction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when age at first sanction is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      ageAtFirstSanction = null,
      dateOfCurrentConviction = LocalDate.of(2018, 1, 1),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    assertNull(error, "Validation should skip when ageAtFirstSanction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when date of birth is null`() {
    val request = RiskScoreRequest(
      dateOfBirth = null,
      ageAtFirstSanction = 18,
      dateOfCurrentConviction = LocalDate.of(2018, 1, 1),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    assertNull(error, "Validation should skip when dateOfBirth is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAgeAtFirstSanction should not add error when date of current conviction is before dateOfBirth`() {
    val request = RiskScoreRequest(
      dateOfBirth = LocalDate.of(2000, 1, 1),
      ageAtFirstSanction = 18,
      dateOfCurrentConviction = LocalDate.of(1999, 10, 12),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    assertNull(error, "Validation should skip when dateOfCurrentConviction is before dateOfBirth")
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
      val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
      assertNull(
        error,
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
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    val expectedError = ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION.asError(
      listOf("dateOfCurrentConviction", "ageAtFirstSanction"),
    )
    assertEquals(expectedError, error)
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAssessmentDate should not add error when dateOfCurrentConviction is null`() {
    val request = RiskScoreRequest(
      assessmentDate = LocalDate.of(2026, 1, 1),
      dateOfCurrentConviction = null,
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request)
    assertNull(error, "Errors list should remain empty when dateOfCurrentConviction is null")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAssessmentDate should not add error when dateOfCurrentConviction is less than 3 months after assessmentDate`() {
    val request = RiskScoreRequest(
      assessmentDate = LocalDate.of(2026, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2026, 2, 1),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request)
    assertNull(
      error,
      "Errors list should remain empty when dateOfCurrentConviction is less than 3 months after assessmentDate",
    )
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAssessmentDate should not add error when dateOfCurrentConviction is exactly 3 months after assessmentDate`() {
    val request = RiskScoreRequest(
      assessmentDate = LocalDate.of(2026, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2026, 4, 1),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request)
    assertNull(error, "Expected no validation errors")
  }

  @Test
  fun `validateDateOfCurrentConvictionAgainstAssessmentDate should add error when dateOfCurrentConviction is more than 3 months after assessmentDate`() {
    val request = RiskScoreRequest(
      assessmentDate = LocalDate.of(2026, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2026, 4, 2),
    )
    val error = commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request)
    val expectedError = ValidationErrorType.DATE_OF_CURRENT_CONVICTION_WITHIN_THREE_MONTHS_OF_ASSESSMENT_DATE.asError(
      listOf("dateOfCurrentConviction", "assessmentDate"),
    )
    assertEquals(expectedError, error)
  }

  @ParameterizedTest
  @MethodSource("test validateDateAtStartOfFollowupAgainstDateOfCurrentConviction logic data")
  fun `test validateDateAtStartOfFollowupAgainstDateOfCurrentConviction logic`(
    dateAtStartOfFollowupCalculated: LocalDate?,
    dateOfCurrentConviction: LocalDate?,
    error: Boolean,
  ) {
    val request = RiskScoreRequest(
      dateAtStartOfFollowupCalculated = dateAtStartOfFollowupCalculated,
      dateOfCurrentConviction = dateOfCurrentConviction,
    )
    val actualError = commonValidator.validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request)

    if (error) {
      assertEquals(
        ValidationError(
          type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED,
          message = "Either Date at start of followup or date of current conviction must be provided",
          fields = listOf("dateAtStartOfFollowupCalculated"),
        ),
        actualError,
      )
    } else {
      assertNull(actualError)
    }
  }

  @ParameterizedTest
  @MethodSource("test validateDateAtStartOfFollowupAgainstDateOfBirth logic data")
  fun `test validateDateAtStartOfFollowupAgainstDateOfBirth logic`(
    dateAtStartOfFollowupCalculated: LocalDate?,
    dateOfBirth: LocalDate?,
    error: Boolean,
  ) {
    val request = RiskScoreRequest(
      dateAtStartOfFollowupCalculated = dateAtStartOfFollowupCalculated,
      dateOfBirth = dateOfBirth,
    )
    val actualError = commonValidator.validateDateAtStartOfFollowupAgainstDateOfBirth(request)

    if (error) {
      assertEquals(
        ValidationError(
          type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH,
          message = "Date of start of followup cannot be before date of birth",
          fields = listOf("dateAtStartOfFollowupCalculated"),
        ),
        actualError,
      )
    } else {
      assertNull(actualError)
    }
  }

  @ParameterizedTest
  @MethodSource("test validateDateAtStartOfFollowupAge logic data")
  fun `test validateDateAtStartOfFollowupAge logic`(
    dateAtStartOfFollowupCalculated: LocalDate?,
    dateOfBirth: LocalDate?,
    error: Boolean,
  ) {
    val request = RiskScoreRequest(
      dateAtStartOfFollowupCalculated = dateAtStartOfFollowupCalculated,
      dateOfBirth = dateOfBirth,
    )
    val actualError = commonValidator.validateDateAtStartOfFollowupAge(request)

    if (error) {
      assertEquals(
        ValidationError(
          type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE,
          message = "Age at date at start of followup must be more than 10 and less than 110",
          fields = listOf("dateAtStartOfFollowupCalculated"),
        ),
        actualError,
      )
    } else {
      assertNull(actualError)
    }
  }

  @ParameterizedTest
  @MethodSource("test validateDateOfMostRecentSexualOffenceAgainstDateOfBirth logic data")
  fun `test validateDateOfMostRecentSexualOffenceAgainstDateOfBirth logic`(
    request: RiskScoreRequest,
    expectedError: ValidationError?,
  ) {
    val error = commonValidator.validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request)
    assertEquals(expectedError, error)
  }

  @Test
  fun `test validateRequiredFields - default (static) with errors`() {
    val request = RiskScoreRequest(
      gender = Gender.FEMALE,
      isUnemployed = true,
      temperControl = ProblemLevel.NO_PROBLEMS,
      suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
    )

    val requiredFields = listOf(
      RiskScoreRequest::dateOfBirth,
      RiskScoreRequest::gender,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::suitabilityOfAccommodation,
      RiskScoreRequest::currentOffenceCode,
    )

    val expectedValidationError = ValidationError(
      type = ValidationErrorType.MISSING_MANDATORY_INPUT,
      message = "Mandatory input field(s) missing",
      fields = listOf("dateOfBirth", "currentOffenceCode"),
    )

    assertEquals(expectedValidationError, commonValidator.validateRequiredFields(request, requiredFields))
  }

  @Test
  fun `test validateRequiredFields - static with errors`() {
    val request = RiskScoreRequest(
      gender = Gender.FEMALE,
      isUnemployed = true,
      temperControl = ProblemLevel.NO_PROBLEMS,
      suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
    )

    val requiredFields = listOf(
      RiskScoreRequest::dateOfBirth,
      RiskScoreRequest::gender,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::suitabilityOfAccommodation,
      RiskScoreRequest::currentOffenceCode,
    )

    val expectedValidationError = ValidationError(
      type = ValidationErrorType.MISSING_MANDATORY_INPUT,
      message = "Mandatory input field(s) missing",
      fields = listOf("dateOfBirth", "currentOffenceCode"),
    )

    assertEquals(
      expectedValidationError,
      commonValidator.validateRequiredFields(request, requiredFields, StaticOrDynamic.STATIC),
    )
  }

  @Test
  fun `test validateRequiredFields - static with no errors`() {
    val request = RiskScoreRequest(
      gender = Gender.FEMALE,
      isUnemployed = true,
      temperControl = ProblemLevel.NO_PROBLEMS,
      suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
    )

    val requiredFields = listOf(
      RiskScoreRequest::gender,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::suitabilityOfAccommodation,
    )

    assertNull(commonValidator.validateRequiredFields(request, requiredFields, StaticOrDynamic.STATIC))
  }

  @Test
  fun `test validateRequiredFields - dynamic with errors`() {
    val request = RiskScoreRequest(
      gender = Gender.FEMALE,
      isUnemployed = true,
      temperControl = ProblemLevel.NO_PROBLEMS,
      suitabilityOfAccommodation = ProblemLevel.SOME_PROBLEMS,
    )

    val requiredFields = listOf(
      RiskScoreRequest::dateOfBirth,
      RiskScoreRequest::gender,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::suitabilityOfAccommodation,
      RiskScoreRequest::currentOffenceCode,
    )

    val expectedValidationError = ValidationError(
      type = ValidationErrorType.MISSING_DYNAMIC_INPUT,
      message = "Dynamic input field(s) missing",
      fields = listOf("dateOfBirth", "currentOffenceCode"),
    )

    assertEquals(
      expectedValidationError,
      commonValidator.validateRequiredFields(request, requiredFields, StaticOrDynamic.DYNAMIC),
    )
  }

  @ParameterizedTest
  @MethodSource("test validateSecondarySexualFields logic data")
  fun `test validateSecondarySexualFields logic`(
    request: RiskScoreRequest,
    expectedValidationError: ValidationError?,
  ) {
    val error = commonValidator.validateSecondarySexualFields(request)

    assertEquals(expectedValidationError, error)
  }

  @ParameterizedTest
  @MethodSource("test validateDateAtStartOfFollowupAgeSexualPredictor logic data")
  fun `test validateDateAtStartOfFollowupAgeSexualPredictor logic`(
    request: RiskScoreRequest,
    expectedValidationError: ValidationError?,
  ) {
    val error = commonValidator.validateDateAtStartOfFollowupAgeSexualPredictor(request)

    assertEquals(expectedValidationError, error)
  }

  @ParameterizedTest
  @MethodSource("test validateSexualSanctionsCount logic data")
  fun `test validateSexualSanctionsCount logic`(
    request: RiskScoreRequest,
    expectedValidationError: ValidationError?,
  ) {
    val error = commonValidator.validateSexualSanctionsCount(request)

    assertEquals(expectedValidationError, error)
  }

  @ParameterizedTest
  @MethodSource("test checkForExistingSexualFields logic data")
  fun `test checkForExistingSexualFields logic`(
    request: RiskScoreRequest,
    expectedValidationError: ValidationError?,
  ) {
    val error = commonValidator.checkForExistingSexualFields(request)

    assertEquals(expectedValidationError, error)
  }

  // Logic Data

  fun `test validateDateOfCurrentConvictionAgainstDateOfBirth logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        dateOfCurrentConviction = null,
        dateOfBirth = LocalDate.of(1995, 1, 1),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        dateOfCurrentConviction = LocalDate.of(2026, 1, 1),
        dateOfBirth = null,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        dateOfCurrentConviction = LocalDate.of(2026, 1, 1),
        dateOfBirth = LocalDate.of(1995, 1, 1),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        dateOfCurrentConviction = LocalDate.of(2000, 1, 1),
        dateOfBirth = LocalDate.of(2000, 1, 1),
      ),
      ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(
        listOf("dateOfCurrentConviction"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        dateOfCurrentConviction = LocalDate.of(2025, 1, 1),
        dateOfBirth = LocalDate.of(2026, 1, 1),
      ),
      ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH.asError(
        listOf("dateOfCurrentConviction"),
      ),
    ),
  )

  fun `test validateDateAtStartOfFollowupAgainstDateOfCurrentConviction logic data`(): Stream<Arguments> = Stream.of(
    // args: dateAtStartOfFollowup, dateOfCurrentConviction, error
    // Both null should result in error
    Arguments.of(null, null, true),
    // All other combinations shouldn't result in an error (i.e. one or both dates provided)
    Arguments.of(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-01"), false),
    Arguments.of(null, LocalDate.parse("2025-01-01"), false),
    Arguments.of(LocalDate.parse("2025-01-01"), null, false),
  )

  fun `test validateDateAtStartOfFollowupAgainstDateOfBirth logic data`(): Stream<Arguments> = Stream.of(
    // args: dateAtStartOfFollowup, dateOfBirth, error
    // Both or one null shouldn't result in an error
    Arguments.of(null, null, false),
    Arguments.of(null, LocalDate.parse("1980-01-01"), false),
    Arguments.of(LocalDate.parse("2025-01-01"), null, false),
    // dateAtStartOfFollowup being after dateOfBirth shouldn't result in an error
    Arguments.of(LocalDate.parse("2026-05-01"), LocalDate.parse("2000-08-12"), false),
    // dateAtStartOfFollowup being before (or equal to) dateOfBirth should result in an error
    Arguments.of(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-07-01"), true),
    Arguments.of(LocalDate.parse("2000-08-12"), LocalDate.parse("2000-08-12"), true),
  )

  fun `test validateDateAtStartOfFollowupAge logic data`(): Stream<Arguments> = Stream.of(
    // args: dateAtStartOfFollowup, dateOfBirth, error
    // Both or one null shouldn't result in an error
    Arguments.of(null, null, false),
    Arguments.of(null, LocalDate.parse("1980-01-01"), false),
    Arguments.of(LocalDate.parse("2025-01-01"), null, false),
    // If date of birth is after dateAtStartOfFollowup, do not error (should have already been caught)
    Arguments.of(LocalDate.parse("2026-05-01"), LocalDate.parse("2026-08-12"), false),
    // An age of less than 110 at dateAtStartOfFollowup shouldn't result in an error
    Arguments.of(LocalDate.parse("2026-05-01"), LocalDate.parse("2000-08-12"), false),
    Arguments.of(LocalDate.parse("2025-08-31"), LocalDate.parse("1915-09-01"), false),
    // An age of 110 or more at dateAtStartOfFollowup should result in an error
    Arguments.of(LocalDate.parse("2025-09-01"), LocalDate.parse("1915-09-01"), true),
    Arguments.of(LocalDate.parse("2025-12-20"), LocalDate.parse("1915-09-01"), true),
    Arguments.of(LocalDate.parse("2026-06-29"), LocalDate.parse("1915-09-01"), true),
    // An age of 10 or less at dateAtStartOfFollowup should result in an error
    Arguments.of(LocalDate.parse("2025-09-01"), LocalDate.parse("2015-09-02"), true),
  )

  fun `test validateDateOfMostRecentSexualOffenceAgainstDateOfBirth logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.of(2026, 1, 1),
        dateOfBirth = null,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.parse("2026-05-01"),
        dateOfBirth = LocalDate.parse("2000-08-12"),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.parse("2025-09-01"),
        dateOfBirth = LocalDate.parse("2015-09-01"),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
        dateOfMostRecentSexualOffence = LocalDate.parse("2025-09-01"),
        dateOfBirth = LocalDate.parse("2015-09-01"),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = null,
        dateOfBirth = LocalDate.of(1995, 1, 1),
      ),
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf("dateOfMostRecentSexualOffence"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.of(2000, 1, 1),
        dateOfBirth = LocalDate.of(2000, 1, 1),
      ),
      ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_BEFORE_DATE_OF_BIRTH.asError(
        listOf("dateOfMostRecentSexualOffence"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.of(2025, 1, 1),
        dateOfBirth = LocalDate.of(2026, 1, 1),
      ),
      ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_BEFORE_DATE_OF_BIRTH.asError(
        listOf("dateOfMostRecentSexualOffence"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.of(2026, 1, 1),
        dateOfBirth = LocalDate.of(2025, 1, 1),
      ),
      ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_OUT_OF_RANGE.asError(
        listOf("dateOfMostRecentSexualOffence"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfMostRecentSexualOffence = LocalDate.of(2135, 1, 2),
        dateOfBirth = LocalDate.of(2025, 1, 1),
      ),
      ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_OUT_OF_RANGE.asError(
        listOf("dateOfMostRecentSexualOffence"),
      ),
    ),
  )

  fun `test validateSecondarySexualFields logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
      ),
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
    ),
  )

  fun `test validateSexualSanctionsCount logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
        totalNumberOfSanctionsForAllOffences = 5,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 0,
        totalContactAdultSexualSanctions = 0,
        totalContactChildSexualSanctions = 0,
        totalNonContactSexualOffences = 0,
      ),
      ValidationErrorType.SEXUAL_REOFFENDING_PREDICTOR_NO_SANCTIONS.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
        totalNumberOfSanctionsForAllOffences = 3,
      ),
      ValidationErrorType.TOTAL_NUMBER_OF_SEXUAL_SANCTIONS_OUT_OF_RANGE.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
    ),
  )

  fun `test checkForExistingSexualFields logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = false,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
        totalIndecentImageSanctions = 1,
        totalContactAdultSexualSanctions = 1,
        totalContactChildSexualSanctions = 1,
        totalNonContactSexualOffences = 1,
      ),
      ValidationErrorType.SEXUAL_REOFFENDING_PREDICTOR_INCONSISTENT_INPUT.asError(
        listOf(
          "hasEverCommittedSexualOffence",
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
    ),
  )

  fun `test validateDateAtStartOfFollowupAgeSexualPredictor logic data`(): Stream<Arguments> = Stream.of(
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.FEMALE,
        hasEverCommittedSexualOffence = true,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = false,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateOfBirth = null,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        hasEverCommittedSexualOffence = true,
        dateAtStartOfFollowupCalculated = null,
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        dateOfBirth = LocalDate.of(1980, 1, 1),
        hasEverCommittedSexualOffence = true,
        dateAtStartOfFollowupCalculated = LocalDate.of(2030, 1, 1),
        dateOfMostRecentSexualOffence = LocalDate.of(1999, 1, 1),
      ),
      null,
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        dateOfBirth = LocalDate.of(1980, 1, 1),
        hasEverCommittedSexualOffence = true,
        dateAtStartOfFollowupCalculated = LocalDate.of(1989, 1, 1),
      ),
      ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(
        listOf("dateAtStartOfFollowupCalculated"),
      ),
    ),
    Arguments.of(
      RiskScoreRequest(
        gender = Gender.MALE,
        dateOfBirth = LocalDate.of(1980, 1, 1),
        hasEverCommittedSexualOffence = true,
        dateAtStartOfFollowupCalculated = LocalDate.of(2090, 1, 1),
      ),
      ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(
        listOf("dateAtStartOfFollowupCalculated"),
      ),
    ),
  )

  @Test
  fun `test validateDrugMisuse - all null`() {
    val request = RiskScoreRequest(
      motivationToTackleDrugMisuse = null,
    )
    val drugQuestions = listOf(
      RiskScoreRequest::hasHeroinUsage,
      RiskScoreRequest::hasCannabisUsage,
      RiskScoreRequest::hasSpiceUsage,
      RiskScoreRequest::hasBenzodiazepinesUsage,
    )

    assertNull(commonValidator.validateDrugMisuse(request, drugQuestions))
  }

  @Test
  fun `test validateDrugMisuse - motivationToTackleDrugMisuse not null`() {
    val request = RiskScoreRequest(
      motivationToTackleDrugMisuse = MotivationLevel.PARTIAL_MOTIVATION,
    )
    val drugQuestions = listOf(
      RiskScoreRequest::hasHeroinUsage,
      RiskScoreRequest::hasCannabisUsage,
      RiskScoreRequest::hasSpiceUsage,
      RiskScoreRequest::hasBenzodiazepinesUsage,
    )

    assertNull(commonValidator.validateDrugMisuse(request, drugQuestions))
  }

  @Test
  fun `test validateDrugMisuse - motivationToTackleDrugMisuse null and some usage false`() {
    val request = RiskScoreRequest(
      motivationToTackleDrugMisuse = null,
      hasPowderCocaineUsage = false,
      hasSteroidsUsage = false,
      hasHallucinogensUsage = false,
    )
    val drugQuestions = listOf(
      RiskScoreRequest::hasPowderCocaineUsage,
      RiskScoreRequest::hasSteroidsUsage,
      RiskScoreRequest::hasHallucinogensUsage,
      RiskScoreRequest::hasSpiceUsage,
      RiskScoreRequest::hasBenzodiazepinesUsage,
    )

    assertNull(commonValidator.validateDrugMisuse(request, drugQuestions))
  }

  @Test
  fun `test validateDrugMisuse - motivationToTackleDrugMisuse null and some usage true`() {
    val request = RiskScoreRequest(
      motivationToTackleDrugMisuse = null,
      hasPowderCocaineUsage = false,
      hasSteroidsUsage = true,
      hasHallucinogensUsage = false,
      hasKetamineUsage = true,
    )
    val drugQuestions = listOf(
      RiskScoreRequest::hasPowderCocaineUsage,
      RiskScoreRequest::hasSteroidsUsage,
      RiskScoreRequest::hasHallucinogensUsage,
      RiskScoreRequest::hasSpiceUsage,
      RiskScoreRequest::hasBenzodiazepinesUsage,
      RiskScoreRequest::hasKetamineUsage,
    )

    val expectedError = ValidationError(
      type = ValidationErrorType.MOTIVATION_TO_TACKLE_DRUG_MISUSE_INCONSISTENT,
      message = "When motivationToTackleDrugMisuse is null, all drug usage questions must be false or null",
      fields = listOf("hasSteroidsUsage", "hasKetamineUsage"),
    )

    assertEquals(expectedError, commonValidator.validateDrugMisuse(request, drugQuestions))
  }
}
