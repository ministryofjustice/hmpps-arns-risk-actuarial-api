package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

@Component
class SeriousViolentReoffendingPredictorValidator(val commonValidator: CommonValidator) : AbstractActuarialValidator(commonValidator) {
  override fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull(
    commonValidator.validateTotalNumberOfSanctionsForAllOffences(request),
    commonValidator.validateTotalNumberOfViolentSanctions(request),
    commonValidator.validateCurrentOffenceCode(request),
    commonValidator.validateAgeAtFirstSanction(request),
    commonValidator.validateDateOfCurrentConvictionAgainstDateOfBirth(request),
    commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request),
    commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request),
  )

  override fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError> = emptyList()

  override fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::assessmentDate,
    RiskScoreRequest::dateOfBirth,
    RiskScoreRequest::dateOfCurrentConviction,
    RiskScoreRequest::ageAtFirstSanction,
    RiskScoreRequest::gender,
    RiskScoreRequest::currentOffenceCode,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
    RiskScoreRequest::totalNumberOfViolentSanctions,
  )

  override fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::didOffenceInvolveCarryingOrUsingWeapon,
    RiskScoreRequest::suitabilityOfAccommodation,
    RiskScoreRequest::isUnemployed,
    RiskScoreRequest::currentAlcoholUseProblems,
    RiskScoreRequest::temperControl,
    RiskScoreRequest::proCriminalAttitudes,
    RiskScoreRequest::previousConvictions,
  )
}
