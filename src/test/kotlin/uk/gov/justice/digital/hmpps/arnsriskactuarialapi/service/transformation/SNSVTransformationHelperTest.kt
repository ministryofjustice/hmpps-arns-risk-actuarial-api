package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.bingeDrinkingProblemWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.chronicDrinkingProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentRelationshipWithPartnerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.didOffenceInvolveCarryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.domesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getAgeAt
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getDomesticViolencePerpetrator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getMonthsSinceLastSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getNumberOfSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getSecondSanctionCasesOnlyWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getThreePlusSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.impulsivityProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.isUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.previousConvictionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.proCriminalAttitudesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.suitabilityOfAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.temperControlWeight
import java.time.LocalDate
import java.time.Month
import java.util.stream.Stream
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SNSVTransformationHelperTest {

  @Test
  fun `didOffenceInvolveCarryingOrUsingWeaponWeight should lookup value`() {
    assertEquals(0.15071282416667, didOffenceInvolveCarryingOrUsingWeaponWeight(true))
    assertEquals(0.0, didOffenceInvolveCarryingOrUsingWeaponWeight(false))
  }

  @Test
  fun `isUnemployedWeight should lookup value`() {
    assertEquals(0.0, isUnemployedWeight(false))
  }

  @Test
  fun `suitabilityOfAccommodationWeight should lookup value`() {
    assertEquals(0.1239420098242586, suitabilityOfAccommodationWeight(ProblemLevel.SIGNIFICANT_PROBLEMS))
  }

  @Test
  fun `currentRelationshipWithPartnerWeight should lookup value`() {
    assertEquals(0.0259107268767618, currentRelationshipWithPartnerWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `domesticViolenceWeight should lookup value`() {
    assertEquals(0.0, domesticViolenceWeight(false))
    assertEquals(0.0847839330659903, domesticViolenceWeight(true))
  }

  @Test
  fun `chronicDrinkingProblemsWeight should lookup value`() {
    assertEquals(0.0, chronicDrinkingProblemsWeight(ProblemLevel.NO_PROBLEMS))
    assertEquals(0.0935672441515258, chronicDrinkingProblemsWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `bingeDrinkingProblemWeight should lookup value`() {
    assertEquals(0.0, bingeDrinkingProblemWeight(ProblemLevel.NO_PROBLEMS))
    assertEquals(0.0567127896345591, bingeDrinkingProblemWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `impulsivityProblemsWeight should lookup value`() {
    assertEquals(0.0, impulsivityProblemsWeight(ProblemLevel.NO_PROBLEMS))
    assertEquals(0.077212834605957, impulsivityProblemsWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `temperControlWeight should lookup value`() {
    assertEquals(0.0, temperControlWeight(ProblemLevel.NO_PROBLEMS))
    assertEquals(0.0482892034688302, temperControlWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `proCriminalAttitudesWeight should lookup value`() {
    assertEquals(0.0, proCriminalAttitudesWeight(ProblemLevel.NO_PROBLEMS))
    assertEquals(0.130830533773332, proCriminalAttitudesWeight(ProblemLevel.SOME_PROBLEMS))
  }

  @Test
  fun `previousConvictionsWeight should lookup value`() {
    assertEquals(0.0, previousConvictionsWeight(emptyList()))
  }

  @Test
  fun `getDomesticViolencePerpetrator should derive from evidenceOfDomesticAbuse and domesticAbuseAgainstPartner`() {
    assertEquals(null, getDomesticViolencePerpetrator(null, null))
    assertEquals(false, getDomesticViolencePerpetrator(false, null))
    assertEquals(null, getDomesticViolencePerpetrator(true, null))
    assertEquals(true, getDomesticViolencePerpetrator(true, true))
    assertEquals(false, getDomesticViolencePerpetrator(true, false))
  }

  @Test
  fun `getAgeGenderPolynomialWeight should calculate correct polynomial weight for example Adam Dynamic`() {
    val dateAtStartOfFollowup = LocalDate.of(2025, 9, 11)
    val dob = dateAtStartOfFollowup.minusYears(33)
    assertEquals(
      -6.368740156140515,
      getAgeGenderPolynomialWeight(
        gender = Gender.MALE,
        dateOfBirth = dob,
        dateAtStartOfFollowup = dateAtStartOfFollowup,
        isSNSVDynamic = true,
      ),
    )
  }

  @Test
  fun `getTotalSanctionWeight should calculate for example Adam Dynamic`() {
    assertEquals(-0.036518584350449, getTotalSanctionWeight(2, true))
  }

  @Test
  fun `getMonthsSinceLastSanctionWeight should calculate for example Adam Dynamic`() {
    val baseline = LocalDate.of(2025, 9, 11)
    val dateAtStartOfFollowup = baseline.minusMonths(5)
    val assessmentDate = baseline
    assertEquals(
      -0.1635176921525733,
      getMonthsSinceLastSanctionWeight(
        dateAtStartOfFollowup = dateAtStartOfFollowup,
        assessmentDate = assessmentDate,
        isSNSVDynamic = true,
      ),
    )
  }

  @Test
  fun `getSecondSanctionCasesOnlyWeight should calculate for example Adam Dynamic`() {
    val ageAtFirstSanction = LocalDate.of(2025, 9, 11)
    val dob = ageAtFirstSanction.minusYears(33)
    val dateOfConviction = ageAtFirstSanction.minusYears(1)

    assertEquals(
      -0.4077429292057845,
      getSecondSanctionCasesOnlyWeight(
        totalNumberOfSanctionsForAllOffences = 2,
        gender = Gender.MALE,
        dateOfBirth = dob,
        dateOfCurrentConviction = dateOfConviction,
        ageAtFirstSanction = 17,
        isSNSVDynamic = true,
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("get2YearInterceptWeightProvider")
  fun `get2YearInterceptWeight should return valid coefficients`(
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = get2YearInterceptWeight(isSNSVDynamic)
    assertEquals(expected, result, 1e-12)
  }

  @ParameterizedTest
  @MethodSource("getAgeGenderPolynomialWeightValidInputProvider")
  fun `getAgeGenderPolynomialWeight should calculate correct polynomial weight`(
    gender: Gender,
    dob: LocalDate,
    assessmentDate: LocalDate,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getAgeGenderPolynomialWeight(gender, dob, assessmentDate, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getAgeGenderPolynomialWeightInvalidInputProvider")
  fun `getAgeGenderPolynomialWeight should throw exception for invalid age`(
    gender: Gender,
    dob: LocalDate,
    assessmentDate: LocalDate,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val ex = assertThrows(IllegalArgumentException::class.java) {
      getAgeGenderPolynomialWeight(gender, dob, assessmentDate, isSNSVDynamic)
    }
    assertEquals(expectedMessage, ex.message)
  }

  @ParameterizedTest
  @MethodSource("getGenderWeightValidInputProvider")
  fun `getGenderWeight should calculate correct polynomial weight`(
    gender: Gender,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getGenderWeight(gender, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getNumberOfSanctionWeightValidInputProvider")
  fun `getNumberOfSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getNumberOfSanctionsWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getNumberOfSanctionWeightInvalidInputProvider")
  fun `getNumberOfSanctionWeight should throw exception for invalid sanction counts`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getNumberOfSanctionsWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    }
    assertEquals(
      "Invalid total number of sanctions value: $totalNumberOfSanctionsForAllOffences",
      exception.message,
    )
  }

  @ParameterizedTest
  @MethodSource("getTotalSanctionWeightValidInputProvider")
  fun `getTotalSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getTotalSanctionWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getTotalSanctionWeightInvalidInputProvider")
  fun `getTotalSanctionWeight should throw exception for invalid sanction counts`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getTotalSanctionWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    }
    assertEquals(
      "Invalid total number of sanctions value: $totalNumberOfSanctionsForAllOffences",
      exception.message,
    )
  }

  @ParameterizedTest
  @MethodSource("getYearsBetweenFirstAndSecondSanctionWeightValidInputProvider")
  fun `getYearsBetweenFirstAndSecondSanctionWeight should calculate correct weight for years between sanctions`(
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    dob: LocalDate,
    convictionDate: LocalDate,
    ageAtFirstSanction: Int,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getSecondSanctionCasesOnlyWeight(
      totalNumberOfSanctionsForAllOffences,
      gender,
      dob,
      convictionDate,
      ageAtFirstSanction,
      isSNSVDynamic,
    )
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getYearsBetweenFirstAndSecondSanctionWeightInvalidInputProvider")
  fun `getYearsBetweenFirstAndSecondSanctionWeight should throw exception for invalid input`(
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    dob: LocalDate,
    convictionDate: LocalDate,
    ageAtFirstSanction: Int,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val ex = assertThrows(IllegalArgumentException::class.java) {
      getSecondSanctionCasesOnlyWeight(
        totalNumberOfSanctionsForAllOffences,
        gender,
        dob,
        convictionDate,
        ageAtFirstSanction,
        isSNSVDynamic,
      )
    }
    assertEquals(expectedMessage, ex.message)
  }

  @ParameterizedTest
  @MethodSource("getMonthsSinceLastSanctionWeightValidInputProvider")
  fun `getMonthsSinceLastSanctionWeight should return correct months since last sanction weight`(
    supervisionStatus: SupervisionStatus,
    dateAtStartOfFollowup: LocalDate,
    assessmentDate: LocalDate,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getMonthsSinceLastSanctionWeight(
      dateAtStartOfFollowup,
      assessmentDate,
      isSNSVDynamic,
    )
    assertEquals(expected, result, 1e-10)
  }

  @ParameterizedTest
  @MethodSource("getThreePlusSanctionsWeightValidInputProvider")
  fun `getThreePlusSanctionsWeight should return correct weight for 3+ sanctions`(
    gender: Gender,
    totalSanctions: Int,
    ageAtFirstSanction: Int,
    dob: LocalDate,
    convictionDate: LocalDate,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getThreePlusSanctionsWeight(
      gender,
      totalSanctions,
      ageAtFirstSanction,
      dob,
      convictionDate,
      isSNSVDynamic,
    )
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getThreePlusSanctionsWeightInvalidInputProvider")
  fun `getThreePlusSanctionsWeight should throw for invalid conviction age`(
    gender: Gender,
    totalSanctions: Int,
    ageAtFirstSanction: Int,
    dob: LocalDate,
    convictionDate: LocalDate,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getThreePlusSanctionsWeight(
        gender,
        totalSanctions,
        ageAtFirstSanction,
        dob,
        convictionDate,
        isSNSVDynamic,
      )
    }
    assertEquals(expectedMessage, exception.message)
  }

  @ParameterizedTest
  @MethodSource("getViolentHistoryWeightValidInputProvider")
  fun `getViolentHistoryWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    gender: Gender,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getViolentHistoryWeight(totalViolentSanctions, gender, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getViolentHistoryWeightInvalidInputProvider")
  fun `getViolentHistoryWeight should throw exception for invalid input`(
    totalViolentSanctions: Int,
    gender: Gender,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getViolentHistoryWeight(totalViolentSanctions, gender, isSNSVDynamic)
    }
    assertEquals(expectedMessage, exception.message)
  }

  @ParameterizedTest
  @MethodSource("getViolentSanctionsWeightValidInputProvider")
  fun `getViolentSanctionsWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getViolentSanctionsWeight(totalViolentSanctions, isSNSVDynamic)
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getViolentSanctionsWeightInvalidInputProvider")
  fun `getViolentSanctionsWeight should throw exception for invalid input`(
    totalViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getViolentSanctionsWeight(totalViolentSanctions, isSNSVDynamic)
    }
    assertEquals(expectedMessage, exception.message)
  }

  @ParameterizedTest
  @MethodSource("getViolenceRateWeightValidInputProvider")
  fun `getViolenceRateWeight should return correct violence rate weight`(
    dateOfBirth: LocalDate,
    dateOfCurrentConviction: LocalDate,
    ageAtFirstSanction: Int,
    totalNumberOfViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expected: Double,
  ) {
    val result = getViolenceRateWeight(
      dateOfBirth,
      dateOfCurrentConviction,
      ageAtFirstSanction,
      totalNumberOfViolentSanctions,
      isSNSVDynamic,
    )
    assertEquals(expected, result, 1e-9)
  }

  @ParameterizedTest
  @MethodSource("getViolenceRateWeightInvalidInputProvider")
  fun `getViolenceRateWeight should throw exception for invalid age at conviction`(
    dateOfBirth: LocalDate,
    dateOfCurrentConviction: LocalDate,
    ageAtFirstSanction: Int,
    totalNumberOfViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expectedMessage: String,
  ) {
    val exception = assertThrows(IllegalArgumentException::class.java) {
      getViolenceRateWeight(
        dateOfBirth,
        dateOfCurrentConviction,
        ageAtFirstSanction,
        totalNumberOfViolentSanctions,
        isSNSVDynamic,
      )
    }
    assertEquals(expectedMessage, exception.message)
  }

  @ParameterizedTest
  @MethodSource("getPreviousConvictionsProvider")
  fun `previousConvictionsWeight should sum correctly`(
    previousConvictions: List<PreviousConviction>,
    expected: Double,
  ) {
    val result = previousConvictionsWeight(previousConvictions)
    assertEquals(expected, result, 1e-9)
  }

  @Test
  fun `out of range values`() {
    val stage = "test stage"
    var exception = assertFailsWith<IllegalArgumentException>(
      block = {
        getAgeAt(stage, LocalDate.of(1990, 1, 1), LocalDate.of(1989, 1, 1), 9)
      },
    )
    assertEquals(exception.message, "Test stage cannot be before date of birth.")
    exception = assertFailsWith<IllegalArgumentException>(
      block = {
        getAgeAt(stage, LocalDate.of(1990, 1, 1), LocalDate.of(2025, 1, 1), 36)
      },
    )
    assertEquals(exception.message, "Age at test stage cannot be less than 36")
  }

  companion object {
    @JvmStatic
    fun get2YearInterceptWeightProvider() = listOf(
      Arguments.of(false, 3.60407707356772),
      Arguments.of(true, 2.39022796091603),
    )

    // getAgeGenderPolynomialWeight method source
    @JvmStatic
    fun getAgeGenderPolynomialWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Male, isSNSVDynamic = true
      Arguments.of(
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateMalePolynomial(30, true),
      ),
      Arguments.of(
        Gender.MALE,
        LocalDate.of(1989, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateMalePolynomial(31, true),
      ),
      // Male, isSNSVDynamic = false
      Arguments.of(
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        calculateMalePolynomial(30, false),
      ),
      // Female, isSNSVDynamic = true
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateFemalePolynomial(30, true),
      ),
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1989, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateFemalePolynomial(31, true),
      ),
      // Female, isSNSVDynamic = false
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        calculateFemalePolynomial(30, false),
      ),
    )

    @JvmStatic
    fun getAgeGenderPolynomialWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        Gender.MALE,
        LocalDate.of(2015, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        "Age at date at start of followup cannot be less than 10",
      ),
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(2025, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        "Date at start of followup cannot be before date of birth.",
      ),
    )

    private fun calculateMalePolynomial(age: Int, isDynamic: Boolean): Double {
      val c1 = if (isDynamic) -0.508802063507919 else -0.546062410902905
      val c2 = if (isDynamic) 0.0153890192629454 else 0.0170043887737295
      val c3 = if (isDynamic) -0.000208800171123703 else -0.000232716989498981
      val c4 = if (isDynamic) 9.83824143383739E-07 else 0.000001094922933981

      return listOf(
        c1 * age,
        c2 * age.toDouble().pow(2),
        c3 * age.toDouble().pow(3),
        c4 * age.toDouble().pow(4),
      ).sum()
    }

    private fun calculateFemalePolynomial(age: Int, isDynamic: Boolean): Double {
      val c1 = if (isDynamic) 1.1436499550056 else 1.09365106131567
      val c2 = if (isDynamic) -0.0448159815299769 else -0.042733609488121
      val c3 = if (isDynamic) 0.000731812620052307 else 0.000697583963826421
      val c4 = if (isDynamic) -4.24504210770651E-06 else -4.04895085233227E-06

      return listOf(
        c1 * age,
        c2 * age.toDouble().pow(2),
        c3 * age.toDouble().pow(3),
        c4 * age.toDouble().pow(4),
      ).sum()
    }

    // getGenderWeightValid method source
    @JvmStatic
    fun getGenderWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(Gender.MALE, true, 0.0),
      Arguments.of(Gender.FEMALE, true, -16.6927292697847),
      Arguments.of(Gender.FEMALE, false, -16.6220270089011),
    )

    // getNumberOfSanctionWeight method source
    @JvmStatic
    fun getNumberOfSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, true, -1.89458617745666),
      Arguments.of(1, false, -2.09447596484765),
      Arguments.of(2, true, -1.51763151836726),
      Arguments.of(2, false, -1.67613460779912),
      Arguments.of(3, true, 0.0),
      Arguments.of(3, false, 0.0),
    )

    @JvmStatic
    fun getNumberOfSanctionWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, true),
      Arguments.of(0, false),
      Arguments.of(-1, true),
      Arguments.of(-1, false),
    )

    // getTotalSanctionWeight method source
    @JvmStatic
    fun getTotalSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, true, -0.0182592921752245),
      Arguments.of(1, false, -0.0147495874606046),
      Arguments.of(2, true, -0.036518584350449),
      Arguments.of(2, false, -0.0294991749212092),
    )

    @JvmStatic
    fun getTotalSanctionWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, true),
      Arguments.of(0, false),
      Arguments.of(-1, true),
      Arguments.of(-1, false),
    )

    // getYearsBetweenFirstAndSecondSanction method source
    @JvmStatic
    fun getYearsBetweenFirstAndSecondSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Male, dynamic
      Arguments.of(
        2,
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        true,
        (30 - 25) * -0.0271828619470523,
      ),
      // Male, static
      Arguments.of(
        2,
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        false,
        (30 - 25) * -0.0292205730647305,
      ),
      // Female, dynamic
      Arguments.of(
        2,
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        true,
        (30 - 25) * -0.0960719132524968,
      ),
      // Female, static
      Arguments.of(
        2,
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        false,
        (30 - 25) * -0.0841673003341906,
      ),
      // number of sanctions not 2
      Arguments.of(1, Gender.FEMALE, LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1), 25, false, 0.0),
    )

    @JvmStatic
    fun getYearsBetweenFirstAndSecondSanctionWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      // Negative years between sanctions
      Arguments.of(
        2,
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2001, 1, 1),
        15,
        true,
        "Years between first and second sanction cannot be a negative",
      ),
      // Age at conviction <= 10
      Arguments.of(
        2,
        Gender.FEMALE,
        LocalDate.of(2015, 1, 1),
        LocalDate.of(2024, 1, 1),
        5,
        false,
        "Age at current conviction date cannot be less than 10",
      ),
      // Age at conviction < 0
      Arguments.of(
        2,
        Gender.MALE,
        LocalDate.of(2030, 1, 1),
        LocalDate.of(2020, 1, 1),
        5,
        true,
        "Current conviction date cannot be before date of birth.",
      ),
    )

    // getMonthsSinceLastSanctionWeight method source
    @JvmStatic
    fun getMonthsSinceLastSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // In custody -> always 0
      Arguments.of(
        SupervisionStatus.CUSTODY,
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JANUARY, 15),
        true,
        0.0,
      ),
      // dateAtStartOfFollowup >= assessmentDate -> 0
      Arguments.of(
        SupervisionStatus.COMMUNITY,
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JANUARY, 1),
        false,
        0.0,
      ),
      // dateAtStartOfFollowup >= assessmentDate -> 0
      Arguments.of(
        SupervisionStatus.COMMUNITY,
        LocalDate.of(2025, Month.JANUARY, 2),
        LocalDate.of(2025, Month.JANUARY, 1),
        false,
        0.0,
      ),
      // Normal calculation for dynamic
      Arguments.of(
        SupervisionStatus.COMMUNITY,
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JUNE, 1),
        true,
        calculateExpected(5, true),
      ),
      // Normal calculation for static
      Arguments.of(
        SupervisionStatus.COMMUNITY,
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.SEPTEMBER, 1),
        false,
        calculateExpected(8, false),
      ),
    )

    private fun calculateExpected(months: Int, isSNSVDynamic: Boolean): Double {
      val c1 = if (isSNSVDynamic) -0.0368447371150021 else -0.038382727965819
      val c2 = if (isSNSVDynamic) 0.000557887384281899 else 0.000548515180678996
      val c3 = if (isSNSVDynamic) 0.0000615531052486415 else 0.0000662558757635182
      val c4 = if (isSNSVDynamic) -1.49652694510477E-06 else -1.59636460181398E-06

      return listOf(
        c1 * months,
        c2 * months.toDouble().pow(2),
        c3 * months.toDouble().pow(3),
        c4 * months.toDouble().pow(4),
      ).sum()
    }

    // getThreePlusSanctionsWeight method source
    @JvmStatic
    fun getThreePlusSanctionsWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Should return 0.0 for < 3 sanctions
      Arguments.of(Gender.MALE, 2, 18, LocalDate.of(1990, 1, 1), LocalDate.of(2025, 1, 1), true, 0.0),

      // Male - dynamic
      Arguments.of(
        Gender.MALE,
        5,
        18,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2025, 1, 1),
        true,
        calculateExpected(Gender.MALE, 5, 18, 35, true),
      ),
      // Male - static
      Arguments.of(
        Gender.MALE,
        6,
        20,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2025, 1, 1),
        false,
        calculateExpected(Gender.MALE, 6, 20, 35, false),
      ),
      // Female - dynamic
      Arguments.of(
        Gender.FEMALE,
        4,
        17,
        LocalDate.of(1985, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateExpected(Gender.FEMALE, 4, 17, 35, true),
      ),
      // Female - static
      Arguments.of(
        Gender.FEMALE,
        4,
        17,
        LocalDate.of(1985, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        calculateExpected(Gender.FEMALE, 4, 17, 35, false),
      ),
    )

    @JvmStatic
    fun getThreePlusSanctionsWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      // Invalid: conviction date before DOB
      Arguments.of(
        Gender.MALE,
        3,
        16,
        LocalDate.of(2000, 1, 1),
        LocalDate.of(1999, 1, 1),
        true,
        "Current conviction date cannot be before date of birth.",
      ),
      // Invalid: conviction age <= 10
      Arguments.of(
        Gender.FEMALE,
        3,
        8,
        LocalDate.of(2015, 1, 1),
        LocalDate.of(2024, 1, 1),
        true,
        "Age at current conviction date cannot be less than 10",
      ),
    )

    private fun calculateExpected(
      gender: Gender,
      totalSanctions: Int,
      ageAtFirstSanction: Int,
      ageAtConviction: Int,
      isSNSVDynamic: Boolean,
    ): Double {
      val x1 = ageAtConviction - ageAtFirstSanction + 12
      val x2 = totalSanctions / x1.toDouble()
      val x3 = ln(x2)

      val c1 = when (gender) {
        Gender.MALE -> if (isSNSVDynamic) 0.689153313085879 else 0.769213898314811
        Gender.FEMALE -> if (isSNSVDynamic) 0.76704149890481 else 0.793186572461819
      }

      return x3 * c1
    }

    // getViolentHistoryWeight method source
    @JvmStatic
    fun getViolentHistoryWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Male - dynamic
      Arguments.of(0, Gender.MALE, true, -0.35940007303088),
      Arguments.of(1, Gender.MALE, true, -0.101514048705338),
      Arguments.of(2, Gender.MALE, true, 0.0),

      // Male - static
      Arguments.of(0, Gender.MALE, false, -0.942816163300621),
      Arguments.of(1, Gender.MALE, false, -0.0633592949212861),
      Arguments.of(2, Gender.MALE, false, 0.0),

      // Female - dynamic
      Arguments.of(0, Gender.FEMALE, true, -1.7513536371131),
      Arguments.of(1, Gender.FEMALE, true, -0.101514048705338),
      Arguments.of(2, Gender.FEMALE, true, 0.0),

      // Female - static
      Arguments.of(0, Gender.FEMALE, false, -2.32321324569237),
      Arguments.of(1, Gender.FEMALE, false, -0.0633592949212861),
      Arguments.of(2, Gender.FEMALE, false, 0.0),
    )

    @JvmStatic
    fun getViolentHistoryWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(-1, Gender.MALE, true, "Invalid total number of violent sanctions value: -1"),
      Arguments.of(-1, Gender.FEMALE, false, "Invalid total number of violent sanctions value: -1"),
    )

    // getViolentSanctionsWeight method source
    @JvmStatic
    fun getViolentSanctionsWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Dynamic
      Arguments.of(0, true, 0.0),
      Arguments.of(1, true, 0.021160895925655),
      Arguments.of(2, true, 0.04232179185131),

      // Static
      Arguments.of(0, false, 0.0),
      Arguments.of(1, false, 0.0188685880078656),
      Arguments.of(2, false, 0.0377371760157312),
    )

    @JvmStatic
    fun getViolentSanctionsWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(-1, true, "Invalid total number of violent sanctions value: -1"),
      Arguments.of(-1, false, "Invalid total number of violent sanctions value: -1"),
    )

    // getViolenceRateWeight method source
    @JvmStatic
    fun getViolenceRateWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Static
      Arguments.of(
        LocalDate.of(1980, 1, 1),
        LocalDate.of(2020, 1, 1),
        18,
        6,
        false,
        ln(6 / (40.0 - 18 + 30)) * 0.207442427665471,
      ),
      // Dynamic
      Arguments.of(
        LocalDate.of(1990, 6, 15),
        LocalDate.of(2025, 6, 15),
        25,
        3,
        true,
        ln(3 / (35.0 - 25 + 30)) * 0.0549319831836878,
      ),
    )

    @JvmStatic
    fun getViolenceRateWeightInvalidInputProvider(): Stream<Arguments> = Stream.of(
      // Age at conviction < 10
      Arguments.of(
        LocalDate.of(2015, Month.JANUARY, 1),
        LocalDate.of(2024, Month.JANUARY, 1),
        5,
        2,
        false,
        "Age at current conviction cannot be less than 10",
      ),
      // Negative age
      Arguments.of(
        LocalDate.of(2030, Month.JANUARY, 1),
        LocalDate.of(2020, Month.JANUARY, 1),
        15,
        2,
        true,
        "Current conviction cannot be before date of birth.",
      ),
    )

    @JvmStatic
    fun getPreviousConvictionsProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(emptyList<PreviousConviction>(), 0.0),
      Arguments.of(
        listOf(
          PreviousConviction.HOMICIDE,
          PreviousConviction.WOUNDING_GBH,
          PreviousConviction.KIDNAPPING,
          PreviousConviction.FIREARMS,
          PreviousConviction.ROBBERY,
          PreviousConviction.AGGRAVATED_BURGLARY,
          PreviousConviction.WEAPON,
          PreviousConviction.CRIMINAL_DAMAGE,
          PreviousConviction.ARSON,
        ),
        0.399463399258737 + 0.451029720739399 + 0.0749101406070305 + 0.218055028351022 +
          0.163248217650296 + 0.506616685297771 + 0.184104582611966 + 0.357345708081477 + 0.12261588608151,
      ),
      Arguments.of(
        listOf(
          PreviousConviction.ARSON,
          PreviousConviction.WEAPON,
          PreviousConviction.ROBBERY,
          PreviousConviction.KIDNAPPING,
          PreviousConviction.HOMICIDE,
        ),
        0.12261588608151 + 0.184104582611966 + 0.163248217650296 + 0.0749101406070305 + 0.399463399258737,
      ),
      Arguments.of(
        listOf(
          PreviousConviction.FIREARMS,
          PreviousConviction.ROBBERY,
          PreviousConviction.AGGRAVATED_BURGLARY,
        ),
        0.218055028351022 + 0.163248217650296 + 0.506616685297771,
      ),
    )
  }
}
