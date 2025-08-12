package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated

interface DomainScore {

  fun getMissingFields(request: PNIRequestValidated): ArrayList<String>

  fun domainNeeds(request: PNIRequestValidated): Int?

  fun projectedNeeds(request: PNIRequestValidated): Int?

  fun overallDomainScore(request: PNIRequestValidated): Triple<Int, Int, List<String>>
}
