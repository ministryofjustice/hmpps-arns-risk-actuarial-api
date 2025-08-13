package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.ln
import kotlin.math.pow

fun get2YearInterceptWeight(isSNSVDynamic: Boolean): Double = when (isSNSVDynamic) {
  false -> 3.60407707356772
  true -> 2.39022796091603
}

fun getAgeGenderPolynomialWeight(gender: Gender, dateOfBirth: LocalDate, assessmentDate: LocalDate, isSNSVDynamic: Boolean): Double {
  if (assessmentDate.isBefore(dateOfBirth)) {
    throw IllegalArgumentException("Assessment date cannot be before date of birth.")
  }
  val currentAge = Period.between(dateOfBirth, assessmentDate).years
  if (currentAge in 0..9) throw IllegalArgumentException("Age at assessment date cannot be less than 10")

  if (gender == Gender.MALE) {
    val c1 = if (isSNSVDynamic) -0.508802063507919 else -0.546062410902905
    val c2 = if (isSNSVDynamic) 0.0153890192629454 else 0.0170043887737295
    val c3 = if (isSNSVDynamic) -0.000208800171123703 else -0.000232716989498981
    val c4 = if (isSNSVDynamic) 9.83824143383739E-07 else 0.000001094922933981

    return listOf(
      c1 * currentAge,
      c2 * currentAge.toDouble().pow(2),
      c3 * currentAge.toDouble().pow(3),
      c4 * currentAge.toDouble().pow(4),
    ).sum()
  } else {
    val c1 = if (isSNSVDynamic) 1.1436499550056 else 1.09365106131567
    val c2 = if (isSNSVDynamic) -0.0448159815299769 else -0.042733609488121
    val c3 = if (isSNSVDynamic) 0.000731812620052307 else 0.000697583963826421
    val c4 = if (isSNSVDynamic) -4.24504210770651E-06 else -4.04895085233227E-06

    return listOf(
      c1 * currentAge,
      c2 * (currentAge.toDouble().pow(2)),
      c3 * (currentAge.toDouble().pow(3)),
      c4 * (currentAge.toDouble().pow(4)),
    ).sum() - 16.6927292697847
  }
}

fun getNumberOfSanctionWeight(totalNumberOfSanctions: Int, isSNSVDynamic: Boolean): Double {
  // OGRS3 contribution
  val c1 = if (isSNSVDynamic) -1.89458617745666 else -2.09447596484765
  val c2 = if (isSNSVDynamic) -1.51763151836726 else -1.67613460779912
  val c3 = if (isSNSVDynamic) -0.0182592921752245 else -0.0147495874606046

  return if (totalNumberOfSanctions == 1) {
    c1
  } else if (totalNumberOfSanctions == 2) {
    c2
  } else if (totalNumberOfSanctions > 2) {
    totalNumberOfSanctions * c3
  } else {
    throw IllegalArgumentException("Invalid total number of sanctions value: $totalNumberOfSanctions")
  }
}

fun getYearsBetweenFirstAndSecondSanctionWeight(gender: Gender, dateOfBirth: LocalDate, dateOfCurrentConviction: LocalDate, ageAtFirstSanction: Int, isSNSVDynamic: Boolean): Double {
  val ageAtCurrentConviction = getAgeDiffAtOffenceDate(dateOfBirth, dateOfCurrentConviction)
  if (ageAtCurrentConviction in 0..9) throw IllegalArgumentException("Age at current conviction cannot be less than 10")
  val yearsBetweenSanctions = ageAtCurrentConviction - ageAtFirstSanction
  if (yearsBetweenSanctions < 0) throw IllegalArgumentException("Years between first and second sanction cannot be a negative")

  if (gender == Gender.MALE) {
    val c1 = if (isSNSVDynamic) -0.0271828619470523 else -0.0292205730647305
    return yearsBetweenSanctions * c1
  } else {
    val c1 = if (isSNSVDynamic) -0.0960719132524968 else -0.0841673003341906
    return yearsBetweenSanctions * c1
  }
}

fun getMonthsSinceLastSanctionWeight(inCustodyOrCommunity: CustodyOrCommunity, dateAtStartOfFollowup: LocalDate, assessmentDate: LocalDate, isSNSVDynamic: Boolean): Double {
  if (inCustodyOrCommunity == CustodyOrCommunity.CUSTODY || assessmentDate >= dateAtStartOfFollowup) {
    return 0.0
  }

  val monthsSinceLastSanction = ChronoUnit.MONTHS.between(assessmentDate, dateAtStartOfFollowup).toInt()

  val c1 = if (isSNSVDynamic) -0.0368447371150021 else -0.038382727965819
  val c2 = if (isSNSVDynamic) 0.000557887384281899 else 0.000548515180678996
  val c3 = if (isSNSVDynamic) 0.0000615531052486415 else 0.0000662558757635182
  val c4 = if (isSNSVDynamic) -1.49652694510477E-06 else -1.59636460181398E-06

  return listOf(
    c1 * monthsSinceLastSanction,
    c2 * (monthsSinceLastSanction.toDouble().pow(2)),
    c3 * (monthsSinceLastSanction.toDouble().pow(3)),
    c4 * (monthsSinceLastSanction.toDouble().pow(4)),
  ).sum()
}

fun getThreePlusSanctionsWeight(gender: Gender, totalNumberOfSanctions: Int, ageAtFirstSanction: Int, dateOfBirth: LocalDate, dateOfCurrentConviction: LocalDate, isSNSVDynamic: Boolean): Double {
  // OGRS4 contribution
  if (totalNumberOfSanctions < 3) return 0.0

  val ageAtCurrentConviction = getAgeDiffAtOffenceDate(dateOfBirth, dateOfCurrentConviction)
  if (ageAtCurrentConviction in 0..9) throw IllegalArgumentException("Age at current conviction cannot be less than 10")

  val x1 = ageAtCurrentConviction - ageAtFirstSanction + 12
  val x2 = totalNumberOfSanctions / x1.toDouble()
  val x3 = ln(x2)

  return if (gender == Gender.MALE) {
    val c1 = if (isSNSVDynamic) 0.689153313085879 else 0.769213898314811
    x3 * c1
  } else {
    val c1 = if (isSNSVDynamic) 0.76704149890481 else 0.76704149890481
    x3 * c1
  }
}

fun getViolentSanctionsWeight(
  totalNumberOfViolentSanctions: Int,
  gender: Gender,
  isSNSVDynamic: Boolean,
): Double {
  val c1 = when (gender) {
    Gender.MALE -> if (isSNSVDynamic) -0.35940007303088 else -0.942816163300621
    Gender.FEMALE -> if (isSNSVDynamic) -1.7513536371131 else -2.32321324569237
  }

  val c2 = if (isSNSVDynamic) -0.101514048705338 else -0.0633592949212861
  val c3 = if (isSNSVDynamic) 0.021160895925655 else 0.0188685880078656

  return when {
    totalNumberOfViolentSanctions == 0 -> c1
    totalNumberOfViolentSanctions == 1 -> c2
    totalNumberOfViolentSanctions > 1 -> c3
    else -> throw IllegalArgumentException("Invalid total number of violent sanctions value: $totalNumberOfViolentSanctions")
  }
}

fun getViolenceRateWeight(dateOfBirth: LocalDate, dateOfCurrentConviction: LocalDate, ageAtFirstSanction: Int, totalNumberOfViolentSanctions: Int, isSNSVDynamic: Boolean): Double {
  val ageAtCurrentConviction = getAgeDiffAtOffenceDate(dateOfBirth, dateOfCurrentConviction)
  if (ageAtCurrentConviction in 0..9) throw IllegalArgumentException("Age at current conviction cannot be less than 10")
  val x1 = ageAtCurrentConviction - ageAtFirstSanction + 30
  val x2 = totalNumberOfViolentSanctions / x1.toDouble()
  val x3 = ln(x2)
  val c1 = if (isSNSVDynamic) 0.0549319831836878 else 0.207442427665471
  return x3 * c1
}
