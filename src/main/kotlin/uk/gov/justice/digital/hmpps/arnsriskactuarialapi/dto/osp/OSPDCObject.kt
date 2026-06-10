package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError

data class OSPDCObject(
    val ospdcBand: RiskBand?,
    val ospdcScore: Double?,
    val pointScore: Int?,
    val ospRiskReduction: Boolean?,
    val femaleVersion: Boolean?,
    val sexualOffenceHistory: Boolean?,
    val validationError: List<ValidationError>?,
    val featureValues: Map<String, Double>?,
)
