package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConvictions
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.SeriousViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.stream.Stream
import kotlin.math.ln
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SeriousViolentReoffendingPredictorTransformationHelperTest {

  @Test
  fun `didOffenceInvolveCarryingOrUsingWeaponWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.CARRY_OR_USE_WEAPON.coefficient, didOffenceInvolveCarryingOrUsingWeaponWeight(true))
    assertEquals(BigDecimal.ZERO, didOffenceInvolveCarryingOrUsingWeaponWeight(false))
  }

  @Test
  fun `isUnemployedWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.UNEMPLOYED.coefficient, isUnemployedWeight(true))
    assertEquals(BigDecimal.ZERO, isUnemployedWeight(false))
  }

  @Test
  fun `suitabilityOfAccommodationWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient * BigDecimal(2), suitabilityOfAccommodationWeight(ProblemLevel.SIGNIFICANT_PROBLEMS))
    assertEquals(SeriousViolentReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient, suitabilityOfAccommodationWeight(ProblemLevel.SOME_PROBLEMS))
    assertThat(suitabilityOfAccommodationWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `chronicDrinkingProblemsWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient * BigDecimal(2), chronicDrinkingProblemsWeight(ProblemLevel.SIGNIFICANT_PROBLEMS))
    assertEquals(SeriousViolentReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient, chronicDrinkingProblemsWeight(ProblemLevel.SOME_PROBLEMS))
    assertThat(chronicDrinkingProblemsWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `temperControlWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.TEMPER.coefficient * BigDecimal(2), temperControlWeight(ProblemLevel.SIGNIFICANT_PROBLEMS))
    assertEquals(SeriousViolentReoffendingPredictorDynamic.TEMPER.coefficient, temperControlWeight(ProblemLevel.SOME_PROBLEMS))
    assertThat(temperControlWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `proCriminalAttitudesWeight should lookup value`() {
    assertEquals(SeriousViolentReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient * BigDecimal(2), proCriminalAttitudesWeight(ProblemLevel.SIGNIFICANT_PROBLEMS))
    assertEquals(SeriousViolentReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient, proCriminalAttitudesWeight(ProblemLevel.SOME_PROBLEMS))
    assertThat(proCriminalAttitudesWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `previousConvictionsWeight should lookup value`() {
    assertEquals(
      listOf(
        PreviousConvictions.HOMICIDE.snsvDynamicWeight,
        PreviousConvictions.WOUNDING_GBH.snsvDynamicWeight,
        PreviousConvictions.KIDNAPPING.snsvDynamicWeight,
        PreviousConvictions.FIREARMS.snsvDynamicWeight,
        PreviousConvictions.ROBBERY.snsvDynamicWeight,
        PreviousConvictions.AGGRAVATED_BURGLARY.snsvDynamicWeight,
        PreviousConvictions.WEAPON.snsvDynamicWeight,
        PreviousConvictions.CRIMINAL_DAMAGE.snsvDynamicWeight,
        PreviousConvictions.ARSON.snsvDynamicWeight,
      ).sumOf { it },
      previousConvictionsWeight(
        listOf(
          PreviousConvictions.HOMICIDE,
          PreviousConvictions.WOUNDING_GBH,
          PreviousConvictions.KIDNAPPING,
          PreviousConvictions.FIREARMS,
          PreviousConvictions.ROBBERY,
          PreviousConvictions.AGGRAVATED_BURGLARY,
          PreviousConvictions.WEAPON,
          PreviousConvictions.CRIMINAL_DAMAGE,
          PreviousConvictions.ARSON,
        ),
      ),
    )

    assertEquals(BigDecimal.ZERO, previousConvictionsWeight(emptyList()))
  }

  @Test
  fun `getAgeGenderPolynomialWeight should calculate correct polynomial weight for example Adam Dynamic`() {
    val dateAtStartOfFollowup = LocalDate.of(2025, 9, 11)
    val dob = dateAtStartOfFollowup.minusYears(33)
    val expectedAge = getAgeAt("date at start of followup", dob, dateAtStartOfFollowup, 10).toBigDecimal()
    val expectedCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
      SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
    )
    val expectedResult = expectedAge * calculatePolynomial(expectedCoefficients, expectedAge)
    assertEquals(
      expectedResult,
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
    assertEquals(SeriousViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient * BigDecimal(2), getTotalSanctionWeight(2, true))
    assertEquals(SeriousViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient * BigDecimal(2), getTotalSanctionWeight(2, false))
  }

  @Test
  fun `getMonthsSinceLastSanctionWeight should calculate for example Adam Dynamic`() {
    val baseline = LocalDate.of(2025, 9, 11)
    val dateAtStartOfFollowup = baseline.minusMonths(5)
    val assessmentDate = baseline
    val dynamicCoefficients = arrayOf(
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
      SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
    )
    val expectedPolynomial = calculatePolynomial(dynamicCoefficients, BigDecimal(5))
    assertEquals(
      BigDecimal(5) * expectedPolynomial,
      getMonthsSinceLastSanctionWeight(
        dateAtStartOfFollowup = dateAtStartOfFollowup,
        assessmentDate = assessmentDate,
        isSNSVDynamic = true,
      ),
    )
  }

  @Test
  fun `getYearsBetweenFirstAndSecondSanctionWeight should calculate for example Adam Dynamic`() {
    val testDate = LocalDate.of(2025, 9, 11)
    val dob = testDate.minusYears(33)
    val dateOfConviction = testDate.minusYears(1)

    assertEquals(
      SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient * BigDecimal(12),
      getYearsBetweenFirstAndSecondSanctionWeight(
        totalNumberOfSanctionsForAllOffences = 2,
        gender = Gender.MALE,
        dateOfBirth = dob,
        dateOfCurrentConviction = dateOfConviction,
        ageAtFirstSanction = 20,
        isSNSVDynamic = true,
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("get2YearInterceptWeightProvider")
  fun `get2YearInterceptWeight should return valid coefficients`(
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = get2YearInterceptWeight(isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeGenderPolynomialWeightValidInputProvider")
  fun `getAgeGenderPolynomialWeight should calculate correct polynomial weight`(
    gender: Gender,
    dob: LocalDate,
    assessmentDate: LocalDate,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getAgeGenderPolynomialWeight(gender, dob, assessmentDate, isSNSVDynamic)
    assertEquals(expected, result)
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
    expected: BigDecimal,
  ) {
    val result = getGenderWeight(gender, isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOneSanctionWeightInputProvider")
  fun `getOneSanctionsWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getOneSanctionsWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTwoSanctionWeightInputProvider")
  fun `getTwoSanctionsWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getTwoSanctionsWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalSanctionWeightValidInputProvider")
  fun `getTotalSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getTotalSanctionWeight(totalNumberOfSanctionsForAllOffences, isSNSVDynamic)
    assertEquals(expected, result)
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
    expected: BigDecimal,
  ) {
    val result = getYearsBetweenFirstAndSecondSanctionWeight(
      totalNumberOfSanctionsForAllOffences,
      gender,
      dob,
      convictionDate,
      ageAtFirstSanction,
      isSNSVDynamic,
    )
    assertEquals(expected, result)
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
      getYearsBetweenFirstAndSecondSanctionWeight(
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
    dateAtStartOfFollowup: LocalDate,
    assessmentDate: LocalDate,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getMonthsSinceLastSanctionWeight(
      dateAtStartOfFollowup,
      assessmentDate,
      isSNSVDynamic,
    )
    assertEquals(expected, result)
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
    expected: BigDecimal,
  ) {
    val result = getThreePlusSanctionsWeight(
      gender,
      totalSanctions,
      ageAtFirstSanction,
      dob,
      convictionDate,
      isSNSVDynamic,
    )
    assertEquals(expected, result)
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
  @MethodSource("getNeverViolentHistoryWeightInputProvider")
  fun `getNeverViolentHistoryWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    gender: Gender,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getNeverViolentHistoryWeight(totalViolentSanctions, gender, isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOnceViolentHistoryWeightInputProvider")
  fun `getOnceViolentHistoryWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getOnceViolentHistoryWeight(totalViolentSanctions, isSNSVDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getViolentSanctionsWeightValidInputProvider")
  fun `getViolentSanctionsWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    isSNSVDynamic: Boolean,
    expected: BigDecimal,
  ) {
    val result = getViolentSanctionsWeight(totalViolentSanctions, isSNSVDynamic)
    assertEquals(expected, result)
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
    expected: BigDecimal,
  ) {
    val result = getViolenceRateWeight(
      dateOfBirth,
      dateOfCurrentConviction,
      ageAtFirstSanction,
      totalNumberOfViolentSanctions,
      isSNSVDynamic,
    )
    assertEquals(expected, result)
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
      Arguments.of(false, SeriousViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient),
      Arguments.of(true, SeriousViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient),
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
        calculateMalePolynomialForGetAgeGenderPolynomialWeight(30, true),
      ),
      Arguments.of(
        Gender.MALE,
        LocalDate.of(1989, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateMalePolynomialForGetAgeGenderPolynomialWeight(31, true),
      ),
      // Male, isSNSVDynamic = false
      Arguments.of(
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        calculateMalePolynomialForGetAgeGenderPolynomialWeight(30, false),
      ),
      // Female, isSNSVDynamic = true
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateFemalePolynomialForGetAgeGenderPolynomialWeight(30, true),
      ),
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1989, 1, 1),
        LocalDate.of(2020, 1, 1),
        true,
        calculateFemalePolynomialForGetAgeGenderPolynomialWeight(31, true),
      ),
      // Female, isSNSVDynamic = false
      Arguments.of(
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        false,
        calculateFemalePolynomialForGetAgeGenderPolynomialWeight(30, false),
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

    private fun calculateMalePolynomialForGetAgeGenderPolynomialWeight(age: Int, isDynamic: Boolean): BigDecimal {
      val c1 = if (isDynamic) SeriousViolentReoffendingPredictorDynamic.AAI_MALE.coefficient else SeriousViolentReoffendingPredictorStatic.AAI_MALE.coefficient
      val c2 = if (isDynamic) SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient else SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient

      return listOf(
        c1 * age.toBigDecimal(),
        c2 * age.toBigDecimal().pow(2),
      ).sumOf { it }
    }

    private fun calculateFemalePolynomialForGetAgeGenderPolynomialWeight(age: Int, isDynamic: Boolean): BigDecimal {
      val c1 = if (isDynamic) SeriousViolentReoffendingPredictorDynamic.AAI_FEMALE.coefficient else SeriousViolentReoffendingPredictorStatic.AAI_FEMALE.coefficient
      val c2 = if (isDynamic) SeriousViolentReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient else SeriousViolentReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient

      return listOf(
        c1 * age.toBigDecimal(),
        c2 * age.toBigDecimal().pow(2),
      ).sumOf { it }
    }

    // getGenderWeightValid method source
    @JvmStatic
    fun getGenderWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(Gender.MALE, true, BigDecimal.ZERO),
      Arguments.of(Gender.FEMALE, true, SeriousViolentReoffendingPredictorDynamic.FEMALE.coefficient),
      Arguments.of(Gender.FEMALE, false, SeriousViolentReoffendingPredictorStatic.FEMALE.coefficient),
    )

    // getOneSanctionWeight method source
    @JvmStatic
    fun getOneSanctionWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, true, SeriousViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient),
      Arguments.of(1, false, SeriousViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient),
      Arguments.of(0, true, BigDecimal.ZERO),
      Arguments.of(2, true, BigDecimal.ZERO),

    )

    // getOneSanctionWeight method source
    @JvmStatic
    fun getTwoSanctionWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(2, false, SeriousViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient),
      Arguments.of(2, true, SeriousViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient),
      Arguments.of(1, false, BigDecimal.ZERO),
      Arguments.of(3, false, BigDecimal.ZERO),
    )

    // getTotalSanctionWeight method source
    @JvmStatic
    fun getTotalSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, true, SeriousViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient),
      Arguments.of(1, false, SeriousViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient),
      Arguments.of(2, true, SeriousViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient * BigDecimal(2)),
      Arguments.of(2, false, SeriousViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient * BigDecimal(2)),
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
        (30 - 25).toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient,
      ),
      // Male, static
      Arguments.of(
        2,
        Gender.MALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        false,
        (30 - 25).toBigDecimal() * SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient,
      ),
      // Female, dynamic
      Arguments.of(
        2,
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        true,
        (30 - 25).toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient,
      ),
      // Female, static
      Arguments.of(
        2,
        Gender.FEMALE,
        LocalDate.of(1990, 1, 1),
        LocalDate.of(2020, 1, 1),
        25,
        false,
        (30 - 25).toBigDecimal() * SeriousViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient,
      ),
      // number of sanctions not 2
      Arguments.of(1, Gender.FEMALE, LocalDate.of(1990, 1, 1), LocalDate.of(2020, 1, 1), 25, false, BigDecimal.ZERO),
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
      // dateAtStartOfFollowup >= assessmentDate -> 0
      Arguments.of(
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JANUARY, 1),
        false,
        BigDecimal.ZERO,
      ),
      // dateAtStartOfFollowup >= assessmentDate -> 0
      Arguments.of(
        LocalDate.of(2025, Month.JANUARY, 2),
        LocalDate.of(2025, Month.JANUARY, 1),
        false,
        BigDecimal.ZERO,
      ),
      // Normal calculation for dynamic
      Arguments.of(
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JUNE, 1),
        true,
        calculateExpected(5, true),
      ),
      // Normal calculation for static
      Arguments.of(
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.SEPTEMBER, 1),
        false,
        calculateExpected(8, false),
      ),
    )

    private fun calculateExpected(months: Int, isSNSVDynamic: Boolean): BigDecimal {
      val c1 = if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient else SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient
      val c2 = if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient else SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient
      val c3 = if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_CUBIC.coefficient else SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient
      val c4 = if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient else SeriousViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient

      return listOf(
        c1 * months.toBigDecimal(),
        c2 * months.toBigDecimal().pow(2),
        c3 * months.toBigDecimal().pow(3),
        c4 * months.toBigDecimal().pow(4),
      ).sumOf { it }
    }

    // getThreePlusSanctionsWeight method source
    @JvmStatic
    fun getThreePlusSanctionsWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Should return 0.0 for < 3 sanctions
      Arguments.of(Gender.MALE, 2, 18, LocalDate.of(1990, 1, 1), LocalDate.of(2025, 1, 1), true, BigDecimal.ZERO),

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
    ): BigDecimal {
      val x1 = ageAtConviction - ageAtFirstSanction + 12
      val x2 = totalSanctions / x1.toDouble()
      val x3 = ln(x2)

      val c1 = when (gender) {
        Gender.MALE -> if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient else SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_MALE.coefficient
        Gender.FEMALE -> if (isSNSVDynamic) SeriousViolentReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient else SeriousViolentReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_V_FEMALE.coefficient
      }

      return x3.toBigDecimal() * c1
    }

    // getViolentHistoryWeight method source
    @JvmStatic
    fun getNeverViolentHistoryWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, Gender.MALE, false, SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_MALE.coefficient),
      Arguments.of(0, Gender.FEMALE, false, SeriousViolentReoffendingPredictorStatic.NEVER_VIOLENT_FEMALE.coefficient),
      Arguments.of(0, Gender.MALE, true, SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_MALE.coefficient),
      Arguments.of(0, Gender.FEMALE, true, SeriousViolentReoffendingPredictorDynamic.NEVER_VIOLENT_FEMALE.coefficient),
      Arguments.of(1, Gender.MALE, true, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getOnceViolentHistoryWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, false, SeriousViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient),
      Arguments.of(1, true, SeriousViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient),
      Arguments.of(0, true, BigDecimal.ZERO),
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
      Arguments.of(0, true, BigDecimal.ZERO),
      Arguments.of(1, true, SeriousViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient),
      Arguments.of(2, true, SeriousViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient * BigDecimal(2)),

      // Static
      Arguments.of(0, false, BigDecimal.ZERO),
      Arguments.of(1, false, SeriousViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient),
      Arguments.of(2, false, SeriousViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient * BigDecimal(2)),
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
        ln(6 / (40.0 - 18 + 30)).toBigDecimal() * SeriousViolentReoffendingPredictorStatic.VIOLENT_RATE.coefficient,
      ),
      // Dynamic
      Arguments.of(
        LocalDate.of(1990, 6, 15),
        LocalDate.of(2025, 6, 15),
        25,
        3,
        true,
        ln(3 / (35.0 - 25 + 30)).toBigDecimal() * SeriousViolentReoffendingPredictorDynamic.VIOLENT_RATE.coefficient,
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
  }
}
