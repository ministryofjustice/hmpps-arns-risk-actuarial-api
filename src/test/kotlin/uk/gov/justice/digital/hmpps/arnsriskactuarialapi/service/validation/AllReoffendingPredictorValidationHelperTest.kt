package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.FIXED_TEST_DATE
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorStaticRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validMinimumAllReoffendingPredictorStaticRiskScoreRequest
import java.time.LocalDate
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AllReoffendingPredictorValidationHelperTest {

  @Test
  fun `validateAllReoffendingPredictorStatic valid static request (all fields set) results in no validation errors`() {
    val errors = validateAllReoffendingPredictorStatic(validAllReoffendingPredictorStaticRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAllReoffendingPredictorStatic valid static request (all mandatory fields set) results in no validation errors`() {
    val errors = validateAllReoffendingPredictorStatic(validMinimumAllReoffendingPredictorStaticRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAllReoffendingPredictorStatic missing all mandatory fields errors`() {
    val request = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      gender = null,
      assessmentDate = FIXED_TEST_DATE,
      dateOfBirth = null,
      dateOfCurrentConviction = null,
      totalNumberOfSanctionsForAllOffences = null,
      ageAtFirstSanction = null,
      currentOffenceCode = null,
      dateAtStartOfFollowupCalculated = null,
    )
    val result = validateAllReoffendingPredictorStatic(request)

    val expectedErrors = listOf(
      ValidationError(
        type = ValidationErrorType.MISSING_MANDATORY_INPUT,
        message = "Mandatory input field(s) missing",
        fields = listOf(
          "dateOfBirth",
          "dateOfCurrentConviction",
          "ageAtFirstSanction",
          "gender",
          "currentOffenceCode",
          "totalNumberOfSanctionsForAllOffences",
        ),
      ),
      ValidationError(
        type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED,
        message = "Either Date at start of followup or date of current conviction must be provided",
        fields = listOf("dateAtStartOfFollowupCalculated"),
      ),
    )

    assertEquals(expectedErrors, result)
  }

  @Test
  fun `validateAllReoffendingPredictorStatic custom validation errors 1`() {
    val request = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      gender = Gender.MALE,
      assessmentDate = LocalDate.parse("2020-01-01"),
      dateOfBirth = LocalDate.parse("2025-01-01"),
      dateOfCurrentConviction = LocalDate.parse("2023-01-01"),
      totalNumberOfSanctionsForAllOffences = 0,
      ageAtFirstSanction = 102,
      currentOffenceCode = "onetwothree",
      dateAtStartOfFollowupCalculated = LocalDate.parse("2200-01-01"),
    )
    val result = validateAllReoffendingPredictorStatic(request)

    val expectedErrors = listOf(
      ValidationError(
        type = ValidationErrorType.DATE_OF_CURRENT_CONVICTION_BEFORE_DATE_OF_BIRTH,
        message = "Date of current conviction cannot be before date of birth",
        fields = listOf("dateOfCurrentConviction"),
      ),
      ValidationError(
        type = ValidationErrorType.DATE_OF_CURRENT_CONVICTION_WITHIN_THREE_MONTHS_OF_ASSESSMENT_DATE,
        message = "Date of current conviction must be less than 3 months after the assessment date",
        fields = listOf("dateOfCurrentConviction", "assessmentDate"),
      ),
      ValidationError(
        type = ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE,
        message = "Age at current conviction must be between 9 and 98 (inclusive)",
        fields = listOf("ageAtFirstSanction"),
      ),
      ValidationError(
        type = ValidationErrorType.OFFENCE_CODE_INCORRECT_FORMAT,
        message = "Offence code must be a string of 5 digits",
        fields = listOf("currentOffenceCode"),
      ),
      ValidationError(
        type = ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE,
        message = "Total number of sanctions must be between 1 and 999 (inclusive)",
        fields = listOf("totalNumberOfSanctionsForAllOffences"),
      ),
      ValidationError(
        type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE,
        message = "Age at date at start of followup must be less than 110",
        fields = listOf("dateAtStartOfFollowupCalculated"),
      ),
    )

    assertEquals(expectedErrors, result)
  }

  @Test
  fun `validateAllReoffendingPredictorStatic custom validation errors 2`() {
    val request = RiskScoreRequest(
      version = RiskScoreVersion.V1_0,
      gender = Gender.MALE,
      assessmentDate = LocalDate.parse("2020-01-01"),
      dateOfBirth = LocalDate.parse("1999-01-01"),
      dateOfCurrentConviction = LocalDate.parse("2020-01-01"),
      totalNumberOfSanctionsForAllOffences = 1,
      ageAtFirstSanction = 23,
      currentOffenceCode = "12345",
      dateAtStartOfFollowupCalculated = LocalDate.parse("1997-01-01"),
    )
    val result = validateAllReoffendingPredictorStatic(request)

    val expectedErrors = listOf(
      ValidationError(
        type = ValidationErrorType.AGE_AT_FIRST_SANCTION_AFTER_AGE_AT_CURRENT_CONVICTION,
        message = "Age at first sanction must be before age at current conviction",
        fields = listOf("dateOfCurrentConviction", "ageAtFirstSanction"),
      ),
      ValidationError(
        type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH,
        message = "Date of start of followup cannot be before date of birth",
        fields = listOf("dateAtStartOfFollowupCalculated"),
      ),
    )

    assertEquals(expectedErrors, result)
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
    val errors = mutableListOf<ValidationError>()

    validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request, errors)

    if (error) {
      assertEquals(
        listOf(
          ValidationError(
            type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED,
            message = "Either Date at start of followup or date of current conviction must be provided",
            fields = listOf("dateAtStartOfFollowupCalculated"),
          ),
        ),
        errors,
      )
    } else {
      assertEquals(emptyList<ValidationError>(), errors)
    }
  }

  fun `test validateDateAtStartOfFollowupAgainstDateOfCurrentConviction logic data`(): Stream<Arguments> = Stream.of(
    // args: dateAtStartOfFollowup, dateOfCurrentConviction, error
    // Both null should result in error
    Arguments.of(null, null, true),
    // All other combinations shouldn't result in an error (i.e. one or both dates provided)
    Arguments.of(LocalDate.parse("2025-01-01"), LocalDate.parse("2025-01-01"), false),
    Arguments.of(null, LocalDate.parse("2025-01-01"), false),
    Arguments.of(LocalDate.parse("2025-01-01"), null, false),
  )

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
    val errors = mutableListOf<ValidationError>()

    validateDateAtStartOfFollowupAgainstDateOfBirth(request, errors)

    if (error) {
      assertEquals(
        listOf(
          ValidationError(
            type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_BEFORE_DATE_OF_BIRTH,
            message = "Date of start of followup cannot be before date of birth",
            fields = listOf("dateAtStartOfFollowupCalculated"),
          ),
        ),
        errors,
      )
    } else {
      assertEquals(emptyList<ValidationError>(), errors)
    }
  }

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
    val errors = mutableListOf<ValidationError>()

    validateDateAtStartOfFollowupAge(request, errors)

    if (error) {
      assertEquals(
        listOf(
          ValidationError(
            type = ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE,
            message = "Age at date at start of followup must be less than 110",
            fields = listOf("dateAtStartOfFollowupCalculated"),
          ),
        ),
        errors,
      )
    } else {
      assertEquals(emptyList<ValidationError>(), errors)
    }
  }

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
  )

  @Test
  fun `validateAllReoffendingPredictorDynamic valid dynamic request results in no validation errors`() {
    val errors = validateAllReoffendingPredictorDynamic(validAllReoffendingPredictorDynamicRiskScoreRequest())
    assertTrue(errors.isEmpty())
  }

  @Test
  fun `validateAllReoffendingPredictorDynamic missing all required fields error`() {
    val request = RiskScoreRequest(
      RiskScoreVersion.V1_0,
      null,
      FIXED_TEST_DATE,
      null,
      null,
      null,
      null,
      null,
      null,
    )
    val result = validateAllReoffendingPredictorDynamic(request)

    val expectedFields = listOf(
      "suitabilityOfAccommodation",
      "isUnemployed",
      "currentRelationshipWithPartner",
      "evidenceOfDomesticAbuse",
      "currentRelationshipStatus",
      "regularOffendingActivities",
      "motivationToTackleDrugMisuse",
      "hasHeroinUsage",
      "hasOtherOpiateUsage",
      "hasCrackCocaineUsage",
      "hasPowderCocaineUsage",
      "hasMisusedPrescriptionDrugUsage",
      "hasBenzodiazepinesUsage",
      "hasCannabisUsage",
      "hasSteroidsUsage",
      "hasOtherDrugsUsage",
      "hasKetamineUsage",
      "hasSpiceUsage",
      "hasHallucinogensUsage",
      "hasSolventsUsage",
      "currentAlcoholUseProblems",
      "excessiveAlcoholUse",
      "impulsivityProblems",
      "proCriminalAttitudes",
    )

    val error = result.first()
    assertEquals(ValidationErrorType.MISSING_DYNAMIC_INPUT, error.type)
    assertEquals("Dynamic input field(s) missing", error.message)
    assertEquals(expectedFields, error.fields)
  }
}
