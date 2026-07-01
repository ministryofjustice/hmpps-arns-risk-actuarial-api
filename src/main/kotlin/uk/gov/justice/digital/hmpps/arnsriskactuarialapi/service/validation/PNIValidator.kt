package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

@Component
class PNIValidator(val commonValidator: CommonValidator) {
  val pniRequiredFields = listOf(
    RiskScoreRequest::supervisionStatus,
  )

  fun validatePNI(request: RiskScoreRequest): List<ValidationError> = listOfNotNull(commonValidator.validateRequiredFields(request, pniRequiredFields))
}
