package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.api

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreVersion
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.lds.LDSObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.mst.MSTObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.opd.OPDObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject

data class RiskScoreResponse(
  val version: RiskScoreVersion,
  val actuarialPredictors: ActuarialPredictorsResponse,
  val lds: LDSObject?,
  val mst: MSTObject?,
  val opd: OPDObject?,
  val pni: PNIObject?,
)
