package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.rsr

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.osp.OSPDCObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject

data class RSRRequestValidated(
  val ospdc: OSPDCObject,
  val ospiic: OSPIICObject,
  val snsv: SNSVObject,
)
