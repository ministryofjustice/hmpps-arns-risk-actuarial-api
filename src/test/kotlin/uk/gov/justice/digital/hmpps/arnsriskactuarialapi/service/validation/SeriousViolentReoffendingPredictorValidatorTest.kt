package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class SeriousViolentReoffendingPredictorValidatorTest {

  @Mock
  private lateinit var commonValidator: CommonValidator

  @InjectMocks
  private lateinit var validator: SeriousViolentReoffendingPredictorValidator

  private val expectedStaticRequiredFields = listOf(
    RiskScoreRequest::assessmentDate,
    RiskScoreRequest::dateOfBirth,
    RiskScoreRequest::dateOfCurrentConviction,
    RiskScoreRequest::ageAtFirstSanction,
    RiskScoreRequest::gender,
    RiskScoreRequest::currentOffenceCode,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
    RiskScoreRequest::totalNumberOfViolentSanctions,
  )

  private val expectedDynamicRequiredFields = listOf(
    RiskScoreRequest::didOffenceInvolveCarryingOrUsingWeapon,
    RiskScoreRequest::suitabilityOfAccommodation,
    RiskScoreRequest::isUnemployed,
    RiskScoreRequest::currentAlcoholUseProblems,
    RiskScoreRequest::temperControl,
    RiskScoreRequest::proCriminalAttitudes,
    RiskScoreRequest::previousConvictions,
  )

  @Test
  fun `test validateStatic`() {
    // Create request object
    val request: RiskScoreRequest = mock()

    val validationError1 = ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("field1", "field2"))
    val validationError2 = ValidationErrorType.AGE_AT_FIRST_SANCTION_OUT_OF_RANGE.asError(listOf("field1"))

    // Mock common validator method calls
    whenever(commonValidator.validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)).thenReturn(validationError1)
    whenever(commonValidator.validateTotalNumberOfSanctionsForAllOffences(request)).thenReturn(null)
    whenever(commonValidator.validateTotalNumberOfViolentSanctions(request)).thenReturn(null)
    whenever(commonValidator.validateCurrentOffenceCode(request)).thenReturn(null)
    whenever(commonValidator.validateAgeAtFirstSanction(request)).thenReturn(validationError2)
    whenever(commonValidator.validateDateOfCurrentConvictionAgainstDateOfBirth(request)).thenReturn(null)
    whenever(commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)).thenReturn(null)
    whenever(commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request)).thenReturn(null)

    // Check that validation errors are returned
    assertEquals(listOf(validationError1, validationError2), validator.validateStatic(request))

    // verify each validation method is called once
    verify(commonValidator).validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)
    verify(commonValidator).validateTotalNumberOfSanctionsForAllOffences(request)
    verify(commonValidator).validateTotalNumberOfViolentSanctions(request)
    verify(commonValidator).validateCurrentOffenceCode(request)
    verify(commonValidator).validateAgeAtFirstSanction(request)
    verify(commonValidator).validateDateOfCurrentConvictionAgainstDateOfBirth(request)
    verify(commonValidator).validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request)
    verify(commonValidator).validateDateOfCurrentConvictionAgainstAssessmentDate(request)
    verifyNoMoreInteractions(commonValidator)
  }

  @Test
  fun `test validateDynamic`() {
    // Create request object
    val request: RiskScoreRequest = mock()

    val validationError1 = ValidationErrorType.MISSING_DYNAMIC_INPUT.asError(listOf("field1", "field2"))

    // Mock common validator method calls
    whenever(commonValidator.validateRequiredFields(request, expectedDynamicRequiredFields, StaticOrDynamic.DYNAMIC)).thenReturn(validationError1)
    // TODO update once further validation logic added

    // Check that validation errors are returned
    assertEquals(listOf(validationError1), validator.validateDynamic(request))

    // verify each validation method is called once
    verify(commonValidator).validateRequiredFields(request, expectedDynamicRequiredFields, StaticOrDynamic.DYNAMIC)
    // TODO update once further validation logic added
    verifyNoMoreInteractions(commonValidator)
  }
}
