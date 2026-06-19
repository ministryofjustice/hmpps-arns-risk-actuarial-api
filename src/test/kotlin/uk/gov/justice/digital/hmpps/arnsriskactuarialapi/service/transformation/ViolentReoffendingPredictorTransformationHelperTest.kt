package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ViolentReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.ViolentReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate

class ViolentReoffendingPredictorTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("get2YearInterceptWeightProvider")
  fun `get2YearInterceptWeight returns correct coefficient based on StaticOrDynamic`(
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.get2YearInterceptWeight(staticOrDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeGenderPolynomialWeightProvider")
  fun `getAgeGenderPolynomialWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    ageAtStartOfFollowup: Int,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight(
      staticOrDynamic,
      gender,
      ageAtStartOfFollowup,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getGenderWeightProvider")
  fun `getGenderWeight returns zero for male and correct coefficient for female based on StaticOrDynamic`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getGenderWeight(staticOrDynamic, gender)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getFirstSanctionWeightProvider")
  fun `getFirstSanctionWeight returns correct coefficient based on StaticOrDynamic only when total sanctions is exactly 1`(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
    expected: BigDecimal,
  ) {
    val result =
      ViolentReoffendingPredictorTransformationHelper.getFirstSanctionWeight(
        staticOrDynamic,
        totalNumberOfSanctionsForAllOffences,
      )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getSecondSanctionWeightProvider")
  fun `getSecondSanctionWeight returns correct coefficient based on StaticOrDynamic only when total sanctions is exactly 2`(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctionsForAllOffences: Int,
    expected: BigDecimal,
  ) {
    val result =
      ViolentReoffendingPredictorTransformationHelper.getSecondSanctionWeight(
        staticOrDynamic,
        totalNumberOfSanctionsForAllOffences,
      )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalSanctionWeightProvider")
  fun `getTotalSanctionWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    totalNumberOfSanctions: Int,
    expected: BigDecimal,
  ) {
    val result =
      ViolentReoffendingPredictorTransformationHelper.getTotalSanctionWeight(staticOrDynamic, totalNumberOfSanctions)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getGapBetweenFirstAndSecondSanctionWeightProvider")
  fun `getGapBetweenFirstAndSecondSanctionWeight returns correct calculated weight based on StaticOrDynamic only if total sanctions is 2`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    totalNumberOfSanctionsForAllOffences: Int,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight(
      staticOrDynamic,
      gender,
      ageAtFirstSanction,
      ageAtCurrentSanction,
      totalNumberOfSanctionsForAllOffences,
    )
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getOffenceFreeMonthsPolynomialWeightProvider")
  fun `getOffenceFreeMonthsPolynomialWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    assessmentDate: LocalDate,
    dateAtStartOfFollowupCalculated: LocalDate,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight(
      staticOrDynamic,
      assessmentDate,
      dateAtStartOfFollowupCalculated,
    )
    assertEquals(expected, result)
  }

  @Test
  fun `getCopasWeight returns zero when total sanction is less than 3`() {
    val result = ViolentReoffendingPredictorTransformationHelper.getCopasVWeight(
      StaticOrDynamic.STATIC,
      1,
      Gender.MALE,
      20,
      30,
    )
    assertEquals(BigDecimal.ZERO, result)
  }

  @ParameterizedTest
  @MethodSource("getCopasVWeightProvider")
  fun `getCopasVWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    totalSanctions: Int,
    ageAtFirst: Int,
    ageAtCurrent: Int,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = ViolentReoffendingPredictorTransformationHelper.getCopasVWeight(
      staticOrDynamic,
      totalSanctions,
      gender,
      ageAtFirst,
      ageAtCurrent,
    )
    assertTrue(expectedWeight.compareTo(actualWeight) == 0) {
      "Expected $expectedWeight but got $actualWeight"
    }
  }

  @ParameterizedTest
  @MethodSource("getCopasViolentOffencesWeightProvider")
  fun `getCopasViolentOffencesWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    totalViolentSanctions: Int,
    ageAtFirst: Int,
    ageAtCurrent: Int,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = ViolentReoffendingPredictorTransformationHelper.getCopasViolentOffencesWeight(
      staticOrDynamic,
      totalViolentSanctions,
      ageAtFirst,
      ageAtCurrent,
    )
    assertTrue(expectedWeight.compareTo(actualWeight) == 0) {
      "Expected $expectedWeight but got $actualWeight"
    }
  }

  @ParameterizedTest
  @MethodSource("getNeverViolentWeightProvider")
  fun `getNeverViolentWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    totalViolentSanctions: Int,
    gender: Gender,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = ViolentReoffendingPredictorTransformationHelper.getNeverViolentWeight(
      staticOrDynamic,
      totalViolentSanctions,
      gender,
    )
    assertTrue(expectedWeight.compareTo(actualWeight) == 0) {
      "Expected $expectedWeight but got $actualWeight"
    }
  }

  @ParameterizedTest
  @MethodSource("getOnceViolentWeightProvider")
  fun `getOnceViolentWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    totalViolentSanctions: Int,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = ViolentReoffendingPredictorTransformationHelper.getOnceViolentWeight(
      staticOrDynamic,
      totalViolentSanctions,
    )
    assertTrue(expectedWeight.compareTo(actualWeight) == 0) {
      "Expected $expectedWeight but got $actualWeight"
    }
  }

  @ParameterizedTest
  @MethodSource("getTotalViolentSanctionsWeightProvider")
  fun `getTotalViolentSanctionsWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = ViolentReoffendingPredictorTransformationHelper.getTotalViolentSanctionsWeight(
      staticOrDynamic,
    )
    assertTrue(expectedWeight.compareTo(actualWeight) == 0) {
      "Expected $expectedWeight but got $actualWeight"
    }
  }

  @Test
  fun `getSuitableAccommodationWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.21086049102561)
    val result =
      ViolentReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getUnemployedWeightProvider")
  fun `getUnemployedWeight returns coefficient if true else zero`(isUnemployed: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getUnemployedWeight(isUnemployed),
    )
  }

  @ParameterizedTest
  @MethodSource("getLiveInRelationshipWeightProvider")
  fun `getLiveInRelationshipWeight returns coefficient only when current relationship status matches in relationship living together`(
    currentRelationshipStatus: CurrentRelationshipStatus,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getLiveInRelationshipWeight(currentRelationshipStatus),
    )
  }

  @Test
  fun `getRelationshipQualityWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0579516555411508)
    val result =
      ViolentReoffendingPredictorTransformationHelper.getRelationshipQualityWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getMultiplicativeRelationshipWeightProvider")
  fun `getMultiplicativeRelationshipWeight returns coefficient only when living together and problem level is not NO_PROBLEMS`(
    currentRelationshipStatus: CurrentRelationshipStatus,
    problemLevel: ProblemLevel,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getMultiplicativeRelationshipWeight(
      currentRelationshipStatus,
      problemLevel,
    )
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getDomesticViolenceWeightProvider")
  fun `getDomesticViolenceWeight returns coefficient if true else zero`(
    evidenceOfDomesticAbuse: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getDomesticViolenceWeight(evidenceOfDomesticAbuse),
    )
  }

  @Test
  fun `getRegularOffendingActivitiesWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.1310982609168522)
    val result =
      ViolentReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getDrugMotivationWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.1133002685303558)
    val result = ViolentReoffendingPredictorTransformationHelper.getDrugMotivationWeight(MotivationLevel.NO_MOTIVATION)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getChronicDrinkingWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.245066358431196)
    val result =
      ViolentReoffendingPredictorTransformationHelper.getChronicDrinkingWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getBingeDrinkingWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.1962252345532608)
    val result =
      ViolentReoffendingPredictorTransformationHelper.getBingeDrinkingWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getImpulsivityWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0647162660315954)
    val result = ViolentReoffendingPredictorTransformationHelper.getImpulsivityWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getTemperControlWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.183809793489406)
    val result = ViolentReoffendingPredictorTransformationHelper.getTemperControlWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getMethadoneUsageWeightProvider")
  fun `getMethadoneUsageWeight returns coefficient if true else zero`(
    hasMethadoneUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getMethadoneUsageWeight(hasMethadoneUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getOtherOpiateUsageWeightProvider")
  fun `getOtherOpiateUsageWeight returns coefficient if true else zero`(
    hasOtherOpiateUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getOtherOpiateUsageWeight(hasOtherOpiateUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getCrackCocaineUsageWeightProvider")
  fun `getCrackCocaineUsageWeight returns coefficient if true else zero`(
    hasCrackCocaineUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getCrackCocaineUsageWeight(hasCrackCocaineUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getPowderCocaineUsageWeightProvider")
  fun `getPowderCocaineUsageWeight returns coefficient if true else zero`(
    hasPowderCocaineUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getPowderCocaineUsageWeight(hasPowderCocaineUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getMisusedPrescriptionDrugUsageWeightProvider")
  fun `getMisusedPrescriptionDrugUsageWeight returns coefficient if true else zero`(
    hasMisusedPrescriptionDrugUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getMisusedPrescriptionDrugUsageWeight(
        hasMisusedPrescriptionDrugUsage,
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("getBenzodiazepinesUsageWeightProvider")
  fun `getBenzodiazepinesUsageWeight returns coefficient if true else zero`(
    hasBenzodiazepinesUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getBenzodiazepinesUsageWeight(hasBenzodiazepinesUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getCannabisUsageWeightProvider")
  fun `getCannabisUsageWeight returns coefficient if true else zero`(hasCannabisUsage: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getCannabisUsageWeight(hasCannabisUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getSteroidsUsageWeightProvider")
  fun `getSteroidsUsageWeight returns coefficient if true else zero`(hasSteroidsUsage: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getSteroidsUsageWeight(hasSteroidsUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getOtherDrugsUsageWeightProvider")
  fun `getOtherDrugsUsageWeight returns coefficient if any parameters are true else zero`(
    hasOtherDrugsUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight(hasOtherDrugsUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getRiskBandProvider")
  fun `getRiskBand returns correct band mapping based on boundaries`(percentageScore: Double, expected: RiskBand) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getRiskBand(percentageScore),
    )
  }

  @ParameterizedTest
  @MethodSource("getRiskBandOutOfBoundsProvider")
  fun `getRiskBand throws exception when percentage score is outside of upper and lower bounds`(percentageScore: Double) {
    assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getRiskBand(percentageScore)
    }
  }

  companion object {
    @JvmStatic
    fun get2YearInterceptWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, ViolentReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, ViolentReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient),
    )

    @JvmStatic
    fun getAgeGenderPolynomialWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        30,
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorStatic.AAI_MALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_CUBIC_MALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_QUARTIC_MALE.coefficient,
          ),
          30.toBigDecimal(),
        ),
      ),

      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        30,
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorDynamic.AAI_MALE.coefficient,
            ViolentReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
          ),
          30.toBigDecimal(),
        ),
      ),

      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        30,
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorStatic.AAI_FEMALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_CUBIC_FEMALE.coefficient,
            ViolentReoffendingPredictorStatic.AAI_QUARTIC_FEMALE.coefficient,
          ),
          30.toBigDecimal(),
        ),
      ),

      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        30,
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
            ViolentReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
            ViolentReoffendingPredictorDynamic.AAI_CUBIC_FEMALE.coefficient,
            ViolentReoffendingPredictorDynamic.AAI_QUARTIC_FEMALE.coefficient,
          ),
          30.toBigDecimal(),
        ),
      ),
    )

    @JvmStatic
    fun getGenderWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, Gender.MALE, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, Gender.FEMALE, ViolentReoffendingPredictorStatic.FEMALE.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, Gender.FEMALE, ViolentReoffendingPredictorDynamic.FEMALE.coefficient),
    )

    @JvmStatic
    fun getFirstSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 3, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 1, ViolentReoffendingPredictorStatic.FIRST_SANCTION.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, ViolentReoffendingPredictorDynamic.FIRST_SANCTION.coefficient),
    )

    @JvmStatic
    fun getSecondSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 2, ViolentReoffendingPredictorStatic.SECOND_SANCTION.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 2, ViolentReoffendingPredictorDynamic.SECOND_SANCTION.coefficient),
    )

    @JvmStatic
    fun getTotalSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, ViolentReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, ViolentReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient),
    )

    @JvmStatic
    fun getGapBetweenFirstAndSecondSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, Gender.MALE, 10, 18, 24, BigDecimal.ZERO),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        2,
        18,
        19,
        ViolentReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        2,
        20,
        24,
        BigDecimal(-0.1795912636573672),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        2,
        19,
        24,
        BigDecimal("-0.262404163556058493689260302517141099087893962860107421875"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        2,
        21,
        23,
        BigDecimal(-0.073747618627718),
      ),
    )

    @JvmStatic
    fun getOffenceFreeMonthsPolynomialWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, LocalDate.of(2024, 12, 12), LocalDate.of(2025, 12, 12), BigDecimal.ZERO),
      Arguments.of(
        StaticOrDynamic.STATIC,
        LocalDate.of(2026, 12, 12),
        LocalDate.of(2025, 12, 12),
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
            ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
            ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
            ViolentReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
          ),
          12.toBigDecimal(),
        ),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        LocalDate.of(2026, 12, 12),
        LocalDate.of(2025, 12, 12),
        calculatePolynomial(
          arrayOf(
            ViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
            ViolentReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
          ),
          12.toBigDecimal(),
        ),
      ),
    )

    @JvmStatic
    fun getCopasVWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        18,
        20,
        30,
        BigDecimal("-0.430166029065360978144182117356508765482203671126626431941986083984375"),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        7,
        24,
        26,
        BigDecimal("-0.704031642020903854107846539767900928552535333437845110893249511718750"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        5,
        18,
        42,
        BigDecimal("-1.379837002501239882629704225188438382332378751016221940517425537109375"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        6,
        21,
        25,
        BigDecimal("-0.7684233620398449673357192651473202005263374303467571735382080078125"),
      ),
    )

    @JvmStatic
    fun getCopasViolentOffencesWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        0,
        20,
        30,
        BigDecimal.ZERO,
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        18,
        20,
        30,
        BigDecimal("-0.36201183964094633649667451877924850833778691594488918781280517578125"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        5,
        18,
        42,
        BigDecimal("-0.9602657079419671634962092933143484874136674989131279289722442626953125"),
      ),
    )

    @JvmStatic
    fun getNeverViolentWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        1,
        Gender.MALE,
        BigDecimal.ZERO,
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        0,
        Gender.MALE,
        BigDecimal("-2.1991202114131898071036630426533520221710205078125"),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        0,
        Gender.FEMALE,
        BigDecimal("-2.809090583506399951829735073260962963104248046875"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        0,
        Gender.MALE,
        BigDecimal("-1.7206770217019400348590352223254740238189697265625"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        0,
        Gender.FEMALE,
        BigDecimal("-2.240649368455450041182075437973253428936004638671875"),
      ),
    )

    @JvmStatic
    fun getOnceViolentWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        5,
        BigDecimal.ZERO,
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        1,
        ViolentReoffendingPredictorStatic.ONCE_VIOLENT.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        1,
        ViolentReoffendingPredictorDynamic.ONCE_VIOLENT.coefficient,
      ),
    )

    @JvmStatic
    fun getTotalViolentSanctionsWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        ViolentReoffendingPredictorStatic.VIOLENT_SANCTIONS.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        ViolentReoffendingPredictorDynamic.VIOLENT_SANCTIONS.coefficient,
      ),
    )

    @JvmStatic
    fun getUnemployedWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.0331815664323161)),
    )

    @JvmStatic
    fun getLiveInRelationshipWeightProvider() = listOf(
      Arguments.of(CurrentRelationshipStatus.NOT_IN_RELATIONSHIP, BigDecimal.ZERO),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ViolentReoffendingPredictorDynamic.LIVE_IN_RELATIONSHIP.coefficient,
      ),
    )

    @JvmStatic
    fun getMultiplicativeRelationshipWeightProvider() = listOf(
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SOME_PROBLEMS,
        ViolentReoffendingPredictorDynamic.QUALITY_OF_LIVE_IN_RELATIONSHIP.coefficient,
      ),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SIGNIFICANT_PROBLEMS,
        BigDecimal(0.235055030489542),
      ),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.NO_PROBLEMS,
        BigDecimal.ZERO,
      ),
      Arguments.of(CurrentRelationshipStatus.NOT_IN_RELATIONSHIP, ProblemLevel.SOME_PROBLEMS, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getDomesticViolenceWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.DOMESTIC_ABUSE.coefficient),
    )

    @JvmStatic
    fun getHeroinUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.182096496867273)),
    )

    @JvmStatic
    fun getMethadoneUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.METHADONE.coefficient),
    )

    @JvmStatic
    fun getOtherOpiateUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.OTHER_OPIATE.coefficient),
    )

    @JvmStatic
    fun getCrackCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.CRACK_COCAINE.coefficient),
    )

    @JvmStatic
    fun getPowderCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.POWDER_COCAINE.coefficient),
    )

    @JvmStatic
    fun getMisusedPrescriptionDrugUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.PRESCRIPTION_DRUG_MISUSE.coefficient),
    )

    @JvmStatic
    fun getBenzodiazepinesUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.BENZODIAZEPINES.coefficient),
    )

    @JvmStatic
    fun getCannabisUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.CANNABIS.coefficient),
    )

    @JvmStatic
    fun getSteroidsUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, ViolentReoffendingPredictorDynamic.STEROIDS.coefficient),
    )

    @JvmStatic
    fun getOtherDrugsUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.150053208890266)),
    )

    @JvmStatic
    fun getRiskBandProvider() = listOf(
      Arguments.of(0.01, RiskBand.LOW),
      Arguments.of(49.99, RiskBand.LOW),
      Arguments.of(50.00, RiskBand.MEDIUM),
      Arguments.of(74.99, RiskBand.MEDIUM),
      Arguments.of(75.00, RiskBand.HIGH),
      Arguments.of(89.99, RiskBand.HIGH),
      Arguments.of(90.00, RiskBand.VERY_HIGH),
      Arguments.of(99.99, RiskBand.VERY_HIGH),
    )

    @JvmStatic
    fun getRiskBandOutOfBoundsProvider() = listOf(
      Arguments.of(0.00),
      Arguments.of(100.00),
    )
  }
}
