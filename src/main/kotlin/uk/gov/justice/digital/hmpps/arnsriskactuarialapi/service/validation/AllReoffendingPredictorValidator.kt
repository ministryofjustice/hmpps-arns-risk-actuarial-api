package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

@Component
class AllReoffendingPredictorValidator(val commonValidator: CommonValidator) : AbstractActuarialValidator(commonValidator) {

  val drugUsageQuestions = listOf(
    RiskScoreRequest::hasHeroinUsage,
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
  )

  override fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull(
    commonValidator.validateDateOfCurrentConvictionAgainstDateOfBirth(request),
    commonValidator.validateDateOfCurrentConvictionAgainstAgeAtFirstSanction(request),
    commonValidator.validateDateOfCurrentConvictionAgainstAssessmentDate(request),
    commonValidator.validateAgeAtFirstSanction(request),
    commonValidator.validateCurrentOffenceCode(request),
    commonValidator.validateTotalNumberOfSanctionsForAllOffences(request),
    commonValidator.validateDateAtStartOfFollowupAgainstDateOfCurrentConviction(request),
    commonValidator.validateDateAtStartOfFollowupAgainstDateOfBirth(request),
    commonValidator.validateDateAtStartOfFollowupAgeUpperLimit(request),
  )

  override fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull(
    commonValidator.validateDrugMisuse(request, drugUsageQuestions),
  )

  override fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::assessmentDate,
    RiskScoreRequest::dateOfBirth,
    RiskScoreRequest::dateOfCurrentConviction,
    RiskScoreRequest::ageAtFirstSanction,
    RiskScoreRequest::gender,
    RiskScoreRequest::currentOffenceCode,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  )

  override fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::suitabilityOfAccommodation,
    RiskScoreRequest::isUnemployed,
    RiskScoreRequest::currentRelationshipWithPartner,
    RiskScoreRequest::evidenceOfDomesticAbuse,
    RiskScoreRequest::currentRelationshipStatus,
    RiskScoreRequest::regularOffendingActivities,
    RiskScoreRequest::currentAlcoholUseProblems,
    RiskScoreRequest::excessiveAlcoholUse,
    RiskScoreRequest::impulsivityProblems,
    RiskScoreRequest::proCriminalAttitudes,
  )
}
