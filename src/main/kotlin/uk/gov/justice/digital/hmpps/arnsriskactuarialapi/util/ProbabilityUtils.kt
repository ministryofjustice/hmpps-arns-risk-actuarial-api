package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.util

import kotlin.math.exp
import kotlin.math.ln

fun getOffenderCopasScore(numberOfPreviousSanctions: Int, ageAtCurrentConviction: Int, ageAtFirstSanction: Int): Double {
  val numerator = 1.plus(numberOfPreviousSanctions).toDouble()
  val denominator = 10.plus(ageAtCurrentConviction).minus(ageAtFirstSanction).toDouble()
  return ln((((numerator / denominator))))
}

fun getReoffendingProbability(totalForAllParameters: Double, x: Double): Double {
  val numerator = exp(x.plus(totalForAllParameters))
  val denominator = 1.plus(numerator)
  return numerator / denominator
}