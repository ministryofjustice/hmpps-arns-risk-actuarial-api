package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.time.LocalDate
import java.time.Period
import java.time.temporal.ChronoUnit
import kotlin.math.ln

class SNSVTransformationHelper {
  companion object {

    const val STATIC_TWO_YEAR_INTERCEPT_WEIGHT = 3.60407707356772
    const val DYNAMIC_TWO_YEAR_INTERCEPT_WEIGHT = 2.39022796091603
    val STATIC_AGE_MALE_COEFFS =
      doubleArrayOf(-0.546062410902905, 0.0170043887737295, -0.000232716989498981, 0.000001094922933981)
    val DYNAMIC_AGE_MALE_COEFFS =
      doubleArrayOf(-0.508802063507919, 0.0153890192629454, -0.000208800171123703, 9.83824143383739E-07)
    val STATIC_AGE_FEMALE_COEFFS =
      doubleArrayOf(1.09365106131567, -0.042733609488121, 0.000697583963826421, -4.04895085233227E-06)
    val DYNAMIC_AGE_FEMALE_COEFFS =
      doubleArrayOf(1.1436499550056, -0.0448159815299769, 0.000731812620052307, -4.24504210770651E-06)
    val STATIC_NUMBER_OF_SANCTIONS_WEIGHTS = doubleArrayOf(-2.09447596484765, -1.67613460779912, -0.0147495874606046)
    val DYNAMIC_NUMBER_OF_SANCTIONS_WEIGHTS = doubleArrayOf(-1.89458617745666, -1.51763151836726, -0.0182592921752245)
    val STATIC_YEARS_BETWEEN_FIRST_AND_SECOND_SANCTIONS_WEIGHTS =
      mapOf(Gender.MALE to -0.0292205730647305, Gender.FEMALE to -0.0841673003341906)
    val DYNAMIC_YEARS_BETWEEN_FIRST_AND_SECOND_SANCTIONS_WEIGHTS =
      mapOf(Gender.MALE to -0.0271828619470523, Gender.FEMALE to -0.0960719132524968)
    const val STATIC_VIOLENCE_RATE_WEIGHT = 0.207442427665471
    const val DYNAMIC_VIOLENCE_RATE_WEIGHT = 0.0549319831836878
    val STATIC_MONTHS_SINCE_LAST_SANCTION_WEIGHTS =
      doubleArrayOf(-0.038382727965819, 0.000548515180678996, 0.0000662558757635182, -1.59636460181398E-06)
    val DYNAMIC_MONTHS_SINCE_LAST_SANCTION_WEIGHTS =
      doubleArrayOf(-0.0368447371150021, 0.000557887384281899, 0.0000615531052486415, -1.49652694510477E-06)
    val STATIC_THREE_PLUS_SANCTIONS_WEIGHT =
      mapOf(Gender.MALE to 0.769213898314811, Gender.FEMALE to 0.76704149890481)
    val DYNAMIC_THREE_PLUS_SANCTIONS_WEIGHT =
      mapOf(Gender.MALE to 0.689153313085879, Gender.FEMALE to 0.76704149890481)
    val STATIC_NON_GENDERED_VIOLENT_SANCTIONS_WEIGHTS = listOfNotNull<Double>(-0.0633592949212861, 0.0188685880078656)
    val DYNAMIC_NON_GENDERED_VIOLENT_SANCTIONS_WEIGHTS = listOfNotNull<Double>(-0.101514048705338, 0.021160895925655)
    val STATIC_GENDERED_VIOLENT_SANCTIONS_WEIGHTS = mapOf(Gender.MALE to -0.942816163300621, Gender.FEMALE to -2.32321324569237)
    val DYNAMIC_GENDERED_VIOLENT_SANCTIONS_WEIGHTS = mapOf(Gender.MALE to -0.35940007303088, Gender.FEMALE to -1.7513536371131)

    fun getAgeAt(stage: String, dateOfBirth: LocalDate, dateAtStage: LocalDate, lowest: Int): Int = Period.between(dateOfBirth, dateAtStage).years
      .apply {
        require(!dateAtStage.isBefore(dateOfBirth)) { "${stage.replaceFirstChar(Char::titlecase)} cannot be before date of birth." }
        require(this >= lowest) { "Age at $stage cannot be less than $lowest" }
      }

    // SNSV Static Transformations

    fun get2YearInterceptWeight(isSNSVDynamic: Boolean): Double = when (isSNSVDynamic) {
      false -> STATIC_TWO_YEAR_INTERCEPT_WEIGHT
      true -> DYNAMIC_TWO_YEAR_INTERCEPT_WEIGHT
    }

    fun getAgeGenderPolynomialWeight(
      gender: Gender,
      dateOfBirth: LocalDate,
      assessmentDate: LocalDate,
      isSNSVDynamic: Boolean,
    ): Double = getAgeAt("assessment date", dateOfBirth, assessmentDate, 10).toDouble()
      .let { x ->
        when (gender) {
          Gender.MALE -> (if (isSNSVDynamic) DYNAMIC_AGE_MALE_COEFFS else STATIC_AGE_MALE_COEFFS).let {
            x * calculatePolynomial(it, x)
          }
          Gender.FEMALE -> (if (isSNSVDynamic) DYNAMIC_AGE_FEMALE_COEFFS else STATIC_AGE_FEMALE_COEFFS).let {
            x * calculatePolynomial(it, x) - 16.6927292697847
          }
        }
      }

    fun getNumberOfSanctionWeight(totalNumberOfSanctionsForAllOffences: Int, isSNSVDynamic: Boolean): Double = // OGRS3 contribution
      when (isSNSVDynamic) {
        true -> DYNAMIC_NUMBER_OF_SANCTIONS_WEIGHTS
        false -> STATIC_NUMBER_OF_SANCTIONS_WEIGHTS
      }.let { weights ->
        when {
          totalNumberOfSanctionsForAllOffences == 1 -> weights[0]
          totalNumberOfSanctionsForAllOffences == 2 -> weights[1]
          totalNumberOfSanctionsForAllOffences > 2 -> totalNumberOfSanctionsForAllOffences * weights[2]
          else -> throw IllegalArgumentException("Invalid total number of sanctions value: $totalNumberOfSanctionsForAllOffences")
        }
      }

    fun getYearsBetweenFirstAndSecondSanctionWeight(
      gender: Gender,
      dateOfBirth: LocalDate,
      dateOfCurrentConviction: LocalDate,
      ageAtFirstSanction: Int,
      isSNSVDynamic: Boolean,
    ): Double {
      val ageAtCurrentConviction = getAgeAt("current conviction date", dateOfBirth, dateOfCurrentConviction, 10)
      val yearsBetweenSanctions = ageAtCurrentConviction - ageAtFirstSanction
      require(yearsBetweenSanctions >= 0) { "Years between first and second sanction cannot be a negative" }
      return yearsBetweenSanctions * when (isSNSVDynamic) {
        true -> DYNAMIC_YEARS_BETWEEN_FIRST_AND_SECOND_SANCTIONS_WEIGHTS
        false -> STATIC_YEARS_BETWEEN_FIRST_AND_SECOND_SANCTIONS_WEIGHTS
      }[gender]!!
    }

    fun getMonthsSinceLastSanctionWeight(supervisionStatus: CustodyOrCommunity, dateAtStartOfFollowup: LocalDate, assessmentDate: LocalDate, isSNSVDynamic: Boolean): Double {
      if (supervisionStatus == CustodyOrCommunity.CUSTODY || assessmentDate >= dateAtStartOfFollowup) {
        return 0.0
      }
      val monthsSinceLastSanction = ChronoUnit.MONTHS.between(assessmentDate, dateAtStartOfFollowup).toInt()
      val coeffs = if (isSNSVDynamic) DYNAMIC_MONTHS_SINCE_LAST_SANCTION_WEIGHTS else STATIC_MONTHS_SINCE_LAST_SANCTION_WEIGHTS
      return monthsSinceLastSanction * calculatePolynomial(coeffs, monthsSinceLastSanction.toDouble())
    }

    fun getThreePlusSanctionsWeight(gender: Gender, totalNumberOfSanctionsForAllOffences: Int, ageAtFirstSanction: Int, dateOfBirth: LocalDate, dateOfCurrentConviction: LocalDate, isSNSVDynamic: Boolean): Double {
      // OGRS4 contribution
      if (totalNumberOfSanctionsForAllOffences < 3) return 0.0

      val ageAtCurrentConviction = getAgeAt("current conviction date", dateOfBirth, dateOfCurrentConviction, 10)
      val x1 = ageAtCurrentConviction - ageAtFirstSanction + 12
      val x2 = totalNumberOfSanctionsForAllOffences / x1.toDouble()
      val x3 = ln(x2)
      return x3 * (if (isSNSVDynamic) DYNAMIC_THREE_PLUS_SANCTIONS_WEIGHT else STATIC_THREE_PLUS_SANCTIONS_WEIGHT)[gender]!!
    }

    fun getViolentSanctionsWeight(
      totalNumberOfViolentSanctions: Int,
      gender: Gender,
      isSNSVDynamic: Boolean,
    ): Double = (
      if (isSNSVDynamic) {
        Pair(DYNAMIC_NON_GENDERED_VIOLENT_SANCTIONS_WEIGHTS, DYNAMIC_GENDERED_VIOLENT_SANCTIONS_WEIGHTS)
      } else {
        Pair(STATIC_NON_GENDERED_VIOLENT_SANCTIONS_WEIGHTS, STATIC_GENDERED_VIOLENT_SANCTIONS_WEIGHTS)
      }
      ).let { (nonGenderedWeights, genderedWeights) ->
      when {
        totalNumberOfViolentSanctions == 0 -> genderedWeights[gender]!!
        totalNumberOfViolentSanctions == 1 -> nonGenderedWeights[0]
        totalNumberOfViolentSanctions > 1 -> nonGenderedWeights[1]
        else -> throw IllegalArgumentException("Invalid total number of violent sanctions value: $totalNumberOfViolentSanctions")
      }
    }

    fun getViolenceRateWeight(
      dateOfBirth: LocalDate,
      dateOfCurrentConviction: LocalDate,
      ageAtFirstSanction: Int,
      totalNumberOfViolentSanctions: Int,
      isSNSVDynamic: Boolean,
    ): Double {
      val ageAtCurrentConviction = getAgeAt("current conviction", dateOfBirth, dateOfCurrentConviction, 10)
      val x1 = ageAtCurrentConviction - ageAtFirstSanction + 30
      val x2 = totalNumberOfViolentSanctions / x1.toDouble()
      val x3 = ln(x2)
      val c1 = if (isSNSVDynamic) DYNAMIC_VIOLENCE_RATE_WEIGHT else STATIC_VIOLENCE_RATE_WEIGHT
      return x3 * c1
    }

    // SNSV Dynamic Additions

    fun didOffenceInvolveCarryingOrUsingWeaponWeight(carryingOrUsingWeapon: Boolean): Double = if (carryingOrUsingWeapon) 0.15071282416667 else 0.0

    fun suitabilityOfAccommodationWeight(suitabilityOfAccommodation: ProblemLevel): Double = 0.0619710049121293 * suitabilityOfAccommodation.score

    fun isUnemployedWeight(isUnemployed: Boolean): Double = if (isUnemployed) 0.0389109699626767 * 2 else 0.0

    fun currentRelationshipWithPartnerWeight(currentRelationshipWithPartner: ProblemLevel): Double = when (currentRelationshipWithPartner) {
      ProblemLevel.NO_PROBLEMS -> 0.0
      ProblemLevel.SOME_PROBLEMS -> 0.0259107268767618
      ProblemLevel.SIGNIFICANT_PROBLEMS -> 0.0935672441515258 * 2
    }

    fun currentAlcoholUseProblemsWeight(currentAlcoholUseProblems: ProblemLevel): Double = when (currentAlcoholUseProblems) {
      ProblemLevel.NO_PROBLEMS -> 0.0
      ProblemLevel.SOME_PROBLEMS -> 0.0935672441515258
      ProblemLevel.SIGNIFICANT_PROBLEMS -> 0.0935672441515258 * 2
    }

    fun excessiveAlcoholUseWeight(excessiveAlcoholUse: ProblemLevel): Double = when (excessiveAlcoholUse) {
      ProblemLevel.NO_PROBLEMS -> 0.0
      ProblemLevel.SOME_PROBLEMS -> 0.0567127896345591
      ProblemLevel.SIGNIFICANT_PROBLEMS -> 0.0567127896345591 * 2
    }

    fun impulsivityProblemsWeight(impulsivityProblems: ProblemLevel): Double = when (impulsivityProblems) {
      ProblemLevel.NO_PROBLEMS -> 0.0
      ProblemLevel.SOME_PROBLEMS -> 0.077212834605957
      ProblemLevel.SIGNIFICANT_PROBLEMS -> 0.077212834605957 * 2
    }

    fun proCriminalAttitudesWeight(proCriminalAttitudes: ProblemLevel): Double = when (proCriminalAttitudes) {
      ProblemLevel.NO_PROBLEMS -> 0.0
      ProblemLevel.SOME_PROBLEMS -> 0.130830533773332
      ProblemLevel.SIGNIFICANT_PROBLEMS -> 0.130830533773332 * 2
    }

    fun domesticViolenceWeight(domesticViolence: Boolean): Double = if (domesticViolence) 0.0847839330659903 else 0.0

    fun previousConvictionsWeight(previousConvictions: List<PreviousConviction>): Double = previousConvictions.sumOf { conviction -> conviction.snsvDynamicWeight }
  }
}
