package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import kotlin.reflect.KProperty1

@Component
class DirectContactSexualReoffendingPredictorValidator(val commonValidator: CommonValidator) : AbstractActuarialValidator(commonValidator) {

  override fun validateStaticCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull(
    commonValidator.validateSecondarySexualFields(request),
    commonValidator.validateTotalNumberOfSanctionsForAllOffencesSexualPredictor(request),
    commonValidator.validateSexualSanctionsCount(request),
    commonValidator.validateDateOfMostRecentSexualOffenceAgainstDateOfBirth(request),
    commonValidator.validateDateAtStartOfFollowupAgeSexualPredictor(request),
    commonValidator.checkForExistingSexualFields(request),
  )

  override fun staticRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::supervisionStatus,
    RiskScoreRequest::hasEverCommittedSexualOffence,
    RiskScoreRequest::dateOfBirth,
    RiskScoreRequest::dateAtStartOfFollowupCalculated,
    RiskScoreRequest::totalNumberOfSanctionsForAllOffences,
  )

  override fun validateDynamicCustom(request: RiskScoreRequest): List<ValidationError> = listOfNotNull()
  override fun dynamicRequiredFields(): List<KProperty1<RiskScoreRequest, Any?>> = listOfNotNull()
}
