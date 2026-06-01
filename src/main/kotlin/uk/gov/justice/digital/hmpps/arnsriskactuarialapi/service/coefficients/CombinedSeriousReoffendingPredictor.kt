package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import java.math.BigDecimal

enum class CombinedSeriousReoffendingPredictor (val label: String, val coefficient: BigDecimal) {

  FEMALE_SEXUAL_OFFENDER("FEMALE_SEXUAL_OFFENDER", BigDecimal(0.0038314176245211));
}
