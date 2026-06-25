package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.imagesandIndirectcontactsexualreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender

sealed interface ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated {
  data class Static(
    val gender: Gender,
    val totalIndecentImageSanctions: Int,
    val totalContactAdultSexualSanctions: Int,
    val totalContactChildSexualSanctions: Int,
    val totalNonContactSexualOffences: Int,
  ) : ImagesAndIndirectContactSexualReoffendingPredictorRequestValidated
}
