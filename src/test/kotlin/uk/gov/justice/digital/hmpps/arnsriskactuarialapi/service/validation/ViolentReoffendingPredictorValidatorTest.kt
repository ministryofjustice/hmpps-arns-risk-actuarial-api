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
class ViolentReoffendingPredictorValidatorTest {

  @Mock
  private lateinit var commonValidator: CommonValidator

  @InjectMocks
  private lateinit var validator: ViolentReoffendingPredictorValidator

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
    RiskScoreRequest::suitabilityOfAccommodation,
    RiskScoreRequest::isUnemployed,
    RiskScoreRequest::currentRelationshipWithPartner,
    RiskScoreRequest::evidenceOfDomesticAbuse,
    RiskScoreRequest::currentRelationshipStatus,
    RiskScoreRequest::regularOffendingActivities,
    RiskScoreRequest::motivationToTackleDrugMisuse,
    RiskScoreRequest::hasOtherOpiateUsage,
    RiskScoreRequest::hasCrackCocaineUsage,
    RiskScoreRequest::hasPowderCocaineUsage,
    RiskScoreRequest::hasMisusedPrescriptionDrugUsage,
    RiskScoreRequest::hasBenzodiazepinesUsage,
    RiskScoreRequest::hasCannabisUsage,
    RiskScoreRequest::hasSteroidsUsage,
    RiskScoreRequest::hasOtherDrugsUsage,
    RiskScoreRequest::hasKetamineUsage,
    RiskScoreRequest::hasSpiceUsage,
    RiskScoreRequest::hasHallucinogensUsage,
    RiskScoreRequest::hasSolventsUsage,
    RiskScoreRequest::hasMethadoneUsage,
    RiskScoreRequest::currentAlcoholUseProblems,
    RiskScoreRequest::excessiveAlcoholUse,
    RiskScoreRequest::temperControl,
  )

  @Test
  fun `test validateStatic`() {
    // Create request object
    val request: RiskScoreRequest = mock()

    val validationError1 = ValidationErrorType.MISSING_MANDATORY_INPUT.asError(listOf("field1", "field2"))

    // Mock common validator method calls
    whenever(commonValidator.validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)).thenReturn(validationError1)
    // TODO update once further validation logic added

    // Check that validation errors are returned
    assertEquals(listOf(validationError1), validator.validateStatic(request))

    // verify each validation method is called once
    verify(commonValidator).validateRequiredFields(request, expectedStaticRequiredFields, StaticOrDynamic.STATIC)
    // TODO update once further validation logic added
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
