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
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ImagesAndIndirectContactSexualReoffendingPredictorValidatorTest {

  @Mock
  private lateinit var commonValidator: CommonValidator

  @InjectMocks
  private lateinit var validator: ImagesAndIndirectContactSexualReoffendingPredictorValidator

  private val expectedStaticRequiredFields = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::hasEverCommittedSexualOffence,
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
    verify(commonValidator, times(1)).validateSexualSanctionsCount(request)
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
      ValidationErrorType.SEXUAL_REOFFENDING_PREDICTOR_NO_SANCTIONS.asError(
        listOf(
          "totalIndecentImageSanctions",
          "totalContactAdultSexualSanctions",
          "totalContactChildSexualSanctions",
          "totalNonContactSexualOffences",
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
      commonValidator.validateSexualSanctionsCount(request),
    ).thenReturn(expectedErrors[2])
    whenever(
      commonValidator.checkForExistingSexualFields(request),
    ).thenReturn(expectedErrors[3])

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
    verify(commonValidator, times(1)).validateSexualSanctionsCount(request)
    verify(commonValidator, times(1)).checkForExistingSexualFields(request)
    verifyNoMoreInteractions(commonValidator)
  }
}
