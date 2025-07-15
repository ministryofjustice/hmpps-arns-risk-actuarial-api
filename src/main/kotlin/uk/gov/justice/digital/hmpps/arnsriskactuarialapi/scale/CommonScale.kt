package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.scale

import java.math.BigDecimal
import java.math.RoundingMode

const val CALCULATION_SCALE = 5

fun BigDecimal.toCommonScale(): BigDecimal = this.setScale(CALCULATION_SCALE, RoundingMode.HALF_UP)
