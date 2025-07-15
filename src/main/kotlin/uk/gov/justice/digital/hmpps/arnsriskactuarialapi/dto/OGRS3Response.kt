package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

data class OGRS3Response(
    val algorithmVersion: AlgorithmVersion?,
    val ogrs3OneYear: Double?,
    val ogrs3TwoYear: Double?,
    val band: RiskBand?,
    val errorMessages: List<ValidationErrorResponse>?,
)
