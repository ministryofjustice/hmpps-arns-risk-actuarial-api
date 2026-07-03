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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
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
    RiskScoreRequest::dateAtStartOfFollowupCalculated,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  )

  @Test
  fun `test validateStatic with missing static required fields`() {
    // Assign
    val request = RiskScoreRequest()

    val validationError =
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf(
          "gender",
          "supervisionStatus",
          "hasEverCommittedSexualOffence",
          "dateOfBirth",
          "dateAtStartOfFollowupCalculated",
          "totalNumberOfSanctionsForAllOffences",
        ),
      )

    whenever(
      commonValidator.validateRequiredFields(
        request,
        expectedStaticRequiredFields,
        StaticOrDynamic.STATIC,
      ),
    ).thenReturn(validationError)

    // Act
    val errors = validator.validateStatic(request)

    // Assert
    assertEquals(listOf(validationError), errors)
    verify(commonValidator, times(1)).validateRequiredFields(
      request,
      expectedStaticRequiredFields,
      StaticOrDynamic.STATIC,
    )
  }

  @Test
  fun `test validateStatic with missing sexual reoffending predictor required fields when hasEverCommittedSexualOffence true`() {
    // Assign
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      dateOfBirth = LocalDate.of(1980, 1, 1),
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = null,
      totalContactChildSexualSanctions = null,
      totalNonContactSexualOffences = null,
      totalIndecentImageSanctions = null,
      dateAtStartOfFollowupCalculated = LocalDate.of(2030, 1, 1),
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = 1,
      supervisionStatus = SupervisionStatus.COMMUNITY,
    )

    val expectedErrors = listOf(
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
        ),
      ),
    )

    // Mock
    whenever(
      commonValidator.validateRequiredFields(
        request,
        expectedStaticRequiredFields,
        StaticOrDynamic.STATIC,
      ),
    ).thenReturn(null)
    whenever(
      commonValidator.validateSexualReoffendingPredictorFields(request, true),
    ).thenReturn(expectedErrors)

    // Act
    val errors = validator.validateStatic(request)

    // Assert
    assertEquals(expectedErrors, errors)
    verify(commonValidator, times(1)).validateRequiredFields(
      request,
      expectedStaticRequiredFields,
      StaticOrDynamic.STATIC,
    )
    verify(commonValidator, times(1)).validateSexualReoffendingPredictorFields(
      request,
      true,
    )
    verifyNoMoreInteractions(commonValidator)
  }

  @Test
  fun `test validateStatic with zero sexual reoffending predictor required fields when hasEverCommittedSexualOffence true`() {
    // Assert
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      dateOfBirth = LocalDate.of(1980, 1, 1),
      hasEverCommittedSexualOffence = true,
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalNonContactSexualOffences = 0,
      totalIndecentImageSanctions = 0,
      dateAtStartOfFollowupCalculated = LocalDate.of(2030, 1, 1),
      dateOfMostRecentSexualOffence = null,
      totalNumberOfSanctionsForAllOffences = 1,
      supervisionStatus = SupervisionStatus.COMMUNITY,
      mostRecentOffenceDate = null,
    )

    val expectedErrors = listOf(
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

    // Mock
    whenever(
      commonValidator.validateRequiredFields(
        request,
        expectedStaticRequiredFields,
        StaticOrDynamic.STATIC,
      ),
    ).thenReturn(null)
    whenever(
      commonValidator.validateSexualReoffendingPredictorFields(
        request,
        true,
      ),
    ).thenReturn(expectedErrors)

    // Act
    val errors = validator.validateStatic(request)

    // Assert
    assertEquals(expectedErrors, errors)
    verify(commonValidator, times(1)).validateRequiredFields(
      request,
      expectedStaticRequiredFields,
      StaticOrDynamic.STATIC,
    )
    verify(commonValidator, times(1)).validateSexualReoffendingPredictorFields(
      request,
      true,
    )
    verifyNoMoreInteractions(commonValidator)
    verifyNoMoreInteractions(commonValidator)
  }
}
