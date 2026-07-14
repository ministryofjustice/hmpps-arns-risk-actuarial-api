package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DirectContactSexualReoffendingPredictorValidatorTest {

  @Mock
  private lateinit var commonValidator: CommonValidator

  @InjectMocks
  private lateinit var validator: DirectContactSexualReoffendingPredictorValidator

  private val expectedStaticRequiredFields = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::supervisionStatus,
    RiskScoreRequest::hasEverCommittedSexualOffence,
    RiskScoreRequest::dateOfBirth,
    RiskScoreRequest::dateAtStartOfFollowup,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  )

  @Test
  fun `test validateStatic with valid request`() {
    // Assign
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      totalIndecentImageSanctions = 1,
      totalContactAdultSexualSanctions = 1,
      totalContactChildSexualSanctions = 1,
      totalNonContactSexualOffences = 1,
      totalNumberOfSanctionsForAllOffences = 4,
      dateOfBirth = LocalDate.of(1980, 1, 1),
      dateOfMostRecentSexualOffence = LocalDate.of(2021, 1, 1),
      dateAtStartOfFollowup = LocalDate.of(2021, 1, 1),
    )

    // Act
    val result = validator.validateStatic(request)

    // Assert
    assertEquals(emptyList(), result)
    verify(commonValidator, times(1)).validateRequiredFields(
      request,
      expectedStaticRequiredFields,
      StaticOrDynamic.STATIC,
    )
    verify(commonValidator, times(1)).validateSecondarySexualFields(request)
    verify(commonValidator, times(1)).validateTotalNumberOfSanctionsForAllOffencesForSexualPredictor(request)
    verify(commonValidator, times(1)).validateSexualSanctionsCount(request)
    verify(commonValidator, times(1)).validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request)
    verify(commonValidator, times(1)).validateDateAtStartOfFollowupAgeForSexualPredictor(request)
    verify(commonValidator, times(1)).checkForExistingSexualFields(request)
    verifyNoMoreInteractions(commonValidator)
  }

  @Test
  fun `test validateStatic with errors`() {
    // Assign
    val request = RiskScoreRequest()

    val expectedErrors = listOf(
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf(
          "gender",
          "hasEverCommittedSexualOffence",
        ),
      ),
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
      ValidationErrorType.TOTAL_NUMBER_OF_SANCTIONS_OUT_OF_RANGE.asError(
        listOf(
          "totalNumberOfSanctionsForAllOffences",
        ),
      ),
      ValidationErrorType.TOTAL_NUMBER_OF_SEXUAL_SANCTIONS_OUT_OF_RANGE.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
      ValidationErrorType.DATE_OF_MOST_RECENT_SEXUAL_OFFENCE_BEFORE_DATE_OF_BIRTH.asError(
        listOf(
          "dateOfMostRecentSexualOffence",
        ),
      ),
      ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_OUT_OF_RANGE.asError(
        listOf(
          "dateAtStartOfFollowup",
        ),
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
    )

    whenever(
      commonValidator.validateRequiredFields(
        request,
        expectedStaticRequiredFields,
        StaticOrDynamic.STATIC,
      ),
    ).thenReturn(expectedErrors[0])
    whenever(
      commonValidator.validateSecondarySexualFields(request),
    ).thenReturn(expectedErrors[1])
    whenever(
      commonValidator.validateTotalNumberOfSanctionsForAllOffencesForSexualPredictor(request),
    ).thenReturn(expectedErrors[2])
    whenever(
      commonValidator.validateSexualSanctionsCount(request),
    ).thenReturn(expectedErrors[3])
    whenever(
      commonValidator.validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request),
    ).thenReturn(expectedErrors[4])
    whenever(
      commonValidator.validateDateAtStartOfFollowupAgeForSexualPredictor(request),
    ).thenReturn(expectedErrors[5])
    whenever(
      commonValidator.checkForExistingSexualFields(request),
    ).thenReturn(expectedErrors[6])

    // Act
    val errors = validator.validateStatic(request)

    // Assert
    assertEquals(expectedErrors, errors)

    verify(commonValidator, times(1)).validateRequiredFields(
      request,
      expectedStaticRequiredFields,
      StaticOrDynamic.STATIC,
    )
    verify(commonValidator, times(1)).validateSecondarySexualFields(request)
    verify(commonValidator, times(1)).validateTotalNumberOfSanctionsForAllOffencesForSexualPredictor(request)
    verify(commonValidator, times(1)).validateSexualSanctionsCount(request)
    verify(commonValidator, times(1)).validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request)
    verify(commonValidator, times(1)).validateDateAtStartOfFollowupAgeForSexualPredictor(request)
    verify(commonValidator, times(1)).checkForExistingSexualFields(request)
    verifyNoMoreInteractions(commonValidator)
  }
}
