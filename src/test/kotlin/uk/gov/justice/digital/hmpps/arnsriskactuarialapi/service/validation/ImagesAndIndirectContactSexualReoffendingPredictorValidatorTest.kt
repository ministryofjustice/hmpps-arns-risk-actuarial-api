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

  private val expectedSexualReoffendingPredictorRequiredFields = listOf(
    RiskScoreRequest::totalIndecentImageSanctions,
    RiskScoreRequest::totalContactAdultSexualSanctions,
    RiskScoreRequest::totalContactChildSexualSanctions,
    RiskScoreRequest::totalNonContactSexualOffences,
  )

  @Test
  fun `test validateStatic with missing static required fields`() {
    // Assign
    val request = RiskScoreRequest()

    val validationError =
      ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("gender", "hasEverCommittedSexualOffence"))

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
      hasEverCommittedSexualOffence = true,
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
      commonValidator.validateImagesAndIndirectSexualFields(
        request,
        expectedSexualReoffendingPredictorRequiredFields,
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
    verify(commonValidator, times(1)).validateImagesAndIndirectSexualFields(
      request,
      expectedSexualReoffendingPredictorRequiredFields,
    )
    verifyNoMoreInteractions(commonValidator)
  }

  @Test
  fun `test validateStatic with zero sexual reoffending predictor required fields when hasEverCommittedSexualOffence true`() {
    // Assert
    val request = RiskScoreRequest(
      gender = Gender.MALE,
      hasEverCommittedSexualOffence = true,
      totalIndecentImageSanctions = 0,
      totalContactAdultSexualSanctions = 0,
      totalContactChildSexualSanctions = 0,
      totalNonContactSexualOffences = 0,
    )

    val expectedErrors = listOf(
      ValidationErrorType.AMBIGUOUS_INPUT.asError(
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
      commonValidator.validateImagesAndIndirectSexualFields(
        request,
        expectedSexualReoffendingPredictorRequiredFields,
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
    verify(commonValidator, times(1)).validateImagesAndIndirectSexualFields(
      request,
      expectedSexualReoffendingPredictorRequiredFields,
    )
    verifyNoMoreInteractions(commonValidator)
    verifyNoMoreInteractions(commonValidator)
  }
}
