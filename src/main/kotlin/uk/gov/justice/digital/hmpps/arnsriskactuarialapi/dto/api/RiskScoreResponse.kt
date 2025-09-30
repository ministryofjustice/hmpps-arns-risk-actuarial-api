package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import com.fasterxml.jackson.annotation.JsonPropertyOrder
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject

@JsonPropertyOrder("version", alphabetic = true)
data class RiskScoreResponse(
  val version: RiskScoreVersion,
  val actuarialPredictors: ActuarialPredictorsResponse,
  val pni: PNIObject?,
  val lds: LDSObject?,
  val opd: OPDObject?,
  val mst: MSTObject?,
)
