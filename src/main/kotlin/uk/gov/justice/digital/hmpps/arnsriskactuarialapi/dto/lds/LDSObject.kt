package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class LDSObject(
    val ldsScore: Int?,
    val validationError: List<ValidationError>?,
)
