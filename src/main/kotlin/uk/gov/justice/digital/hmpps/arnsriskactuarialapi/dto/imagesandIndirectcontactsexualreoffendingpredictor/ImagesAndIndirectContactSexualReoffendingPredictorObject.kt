package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import java.math.BigDecimal

data class ImagesAndIndirectContactSexualReoffendingPredictorObject(
  val score: Double?,
  val band: RiskBand?,
  val staticOrDynamic: StaticOrDynamic?,
  var validationErrors: List<ValidationError>?,
  val featureValues: Map<String, BigDecimal>?,
)
