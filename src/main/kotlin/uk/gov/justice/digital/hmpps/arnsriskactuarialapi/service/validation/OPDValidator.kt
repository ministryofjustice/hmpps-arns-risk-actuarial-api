package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import kotlin.reflect.KProperty1

@Component
class OPDValidator(val commonValidator: CommonValidator) {
  val OPD_REQUIRED_FIELDS: List<KProperty1<RiskScoreRequest, Any?>> = listOf(
    RiskScoreRequest::gender,
    RiskScoreRequest::overallRiskForAssessment,
    RiskScoreRequest::highestRiskLevelOverAllAssessments,
    RiskScoreRequest::currentOffenceCode,
    RiskScoreRequest::hasCustodialSentence,
  )

  fun validateOPD(request: RiskScoreRequest): List<ValidationError> {
    val errors = mutableListOf<ValidationError>()
    validateRequiredFields(request, errors)
    validateDomesticAbuse(request, errors)
    commonValidator.validateCurrentOffenceCode(request)?.let { errors.add(it) }
    return errors
  }

  private fun validateDomesticAbuse(
    request: RiskScoreRequest,
    errors: MutableList<ValidationError>,
  ) {
    val hasNoEvidenceOfDomesticAbuse = request.evidenceOfDomesticAbuse == null || !request.evidenceOfDomesticAbuse
    val hasDomesticAbuseFieldsNotNull = request.domesticAbuseAgainstPartner != null || request.domesticAbuseAgainstFamily != null
    if (hasNoEvidenceOfDomesticAbuse && hasDomesticAbuseFieldsNotNull) {
      errors += ValidationErrorType.DOMESTIC_ABUSE_INCONSISTENT_INPUT.asError(
        listOf(
          RiskScoreRequest::evidenceOfDomesticAbuse.name,
          RiskScoreRequest::domesticAbuseAgainstFamily.name,
          RiskScoreRequest::domesticAbuseAgainstPartner.name,
        ),
      )
    }
  }

  private fun validateRequiredFields(
    request: RiskScoreRequest,
    errors: MutableList<ValidationError>,
  ) {
    val missingFields = arrayListOf<String>()
    OPD_REQUIRED_FIELDS.forEach { missingFields.addIfNull(request, it) }
    if (request.isEligibleForMappa == null && request.gender == Gender.FEMALE) missingFields.add(RiskScoreRequest::isEligibleForMappa.name)
    if (request.evidenceOfDomesticAbuse == true) {
      missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstPartner)
      missingFields.addIfNull(request, RiskScoreRequest::domesticAbuseAgainstFamily)
    }

    if (missingFields.isNotEmpty()) {
      errors += ValidationErrorType.MISSING_MANDATORY_INPUT.asError(missingFields)
    }
  }
}
