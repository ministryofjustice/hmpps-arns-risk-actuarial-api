package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

data class OSPIICOutput(
  val band: RiskBand?,
  val score: Double?,
)
