package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.*
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.ln
import kotlin.math.pow

class AllReoffendingPredictorTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("get2YearInterceptWeightProvider")
  fun `get2YearInterceptWeight returns correct coefficient based on StaticOrDynamic`(
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = AllReoffendingPredictorTransformationHelper.get2YearInterceptWeight(staticOrDynamic)
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
    val result = AllReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight(
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
    val result = AllReoffendingPredictorTransformationHelper.getGenderWeight(staticOrDynamic, gender)
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
      AllReoffendingPredictorTransformationHelper.getFirstSanctionWeight(
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
      AllReoffendingPredictorTransformationHelper.getSecondSanctionWeight(
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
      AllReoffendingPredictorTransformationHelper.getTotalSanctionWeight(staticOrDynamic, totalNumberOfSanctions)
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
    val result = AllReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight(
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
    val result = AllReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight(
      staticOrDynamic,
      assessmentDate,
      dateAtStartOfFollowupCalculated,
    )
    assertEquals(expected, result)
  }

  @Test
  fun `getCopasWeight returns zero when total sanction is less than 3`() {
    val result = AllReoffendingPredictorTransformationHelper.getCopasWeight(
      StaticOrDynamic.STATIC,
      1,
      Gender.MALE,
      20,
      30,
    )
    assertEquals(BigDecimal.ZERO, result)
  }

  @ParameterizedTest
  @MethodSource("getCopasWeightProvider")
  fun `getCopasWeight returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    totalSanctions: Int,
    ageAtFirst: Int,
    ageAtCurrent: Int,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = AllReoffendingPredictorTransformationHelper.getCopasWeight(
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

  @Test
  fun `getCopasSquaredWeight returns zero when total sanction is less than 3`() {
    val result = AllReoffendingPredictorTransformationHelper.getCopasSquaredWeight(
      StaticOrDynamic.STATIC,
      1,
      Gender.MALE,
      20,
      30,
    )
    assertEquals(BigDecimal.ZERO, result)
  }

  @ParameterizedTest
  @MethodSource("getCopasSquaredWeightProvider")
  fun ` returns correct calculated weight`(
    staticOrDynamic: StaticOrDynamic,
    gender: Gender,
    totalSanctions: Int,
    ageAtFirst: Int,
    ageAtCurrent: Int,
    expectedWeight: BigDecimal,
  ) {
    val actualWeight = AllReoffendingPredictorTransformationHelper.getCopasSquaredWeight(
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

  @Test
  fun `getSuitableAccommodationWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.1696098316711506)
    val result =
      AllReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getUnemployedWeightProvider")
  fun `getUnemployedWeight returns coefficient if true else zero`(isUnemployed: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getUnemployedWeight(isUnemployed),
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
      AllReoffendingPredictorTransformationHelper.getLiveInRelationshipWeight(currentRelationshipStatus),
    )
  }

  @Test
  fun `getRelationshipQualityWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0728103770010276)
    val result =
      AllReoffendingPredictorTransformationHelper.getRelationshipQualityWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
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
    val result = AllReoffendingPredictorTransformationHelper.getMultiplicativeRelationshipWeight(
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
      AllReoffendingPredictorTransformationHelper.getDomesticViolenceWeight(evidenceOfDomesticAbuse),
    )
  }

  @Test
  fun `getRegularOffendingActivitiesWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.252983205184058)
    val result =
      AllReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getDrugMotivationWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.17230849986786)
    val result = AllReoffendingPredictorTransformationHelper.getDrugMotivationWeight(MotivationLevel.NO_MOTIVATION)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getChronicDrinkingWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.1490013407203544)
    val result = AllReoffendingPredictorTransformationHelper.getChronicDrinkingWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getBingeDrinkingWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0676854872930216)
    val result = AllReoffendingPredictorTransformationHelper.getBingeDrinkingWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getImpulsivityWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0843976740190594)
    val result = AllReoffendingPredictorTransformationHelper.getImpulsivityWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @Test
  fun `getProCriminalAttitudeWeight returns correct calculated weight`() {
    val expected = BigDecimal(0.0878166190882834)
    val result =
      AllReoffendingPredictorTransformationHelper.getProCriminalAttitudeWeight(ProblemLevel.SIGNIFICANT_PROBLEMS)
    assertTrue(expected.compareTo(result) == 0) {
      "Expected $expected but got $result"
    }
  }

  @ParameterizedTest
  @MethodSource("getHeroinUsageWeightProvider")
  fun `getHeroinUsageWeight returns coefficient if true else zero`(hasHeroinUsage: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getHeroinUsageWeight(hasHeroinUsage),
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
      AllReoffendingPredictorTransformationHelper.getOtherOpiateUsageWeight(hasOtherOpiateUsage),
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
      AllReoffendingPredictorTransformationHelper.getCrackCocaineUsageWeight(hasCrackCocaineUsage),
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
      AllReoffendingPredictorTransformationHelper.getPowderCocaineUsageWeight(hasPowderCocaineUsage),
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
      AllReoffendingPredictorTransformationHelper.getMisusedPrescriptionDrugUsageWeight(hasMisusedPrescriptionDrugUsage),
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
      AllReoffendingPredictorTransformationHelper.getBenzodiazepinesUsageWeight(hasBenzodiazepinesUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getCannabisUsageWeightProvider")
  fun `getCannabisUsageWeight returns coefficient if true else zero`(hasCannabisUsage: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getCannabisUsageWeight(hasCannabisUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getSteroidsUsageWeightProvider")
  fun `getSteroidsUsageWeight returns coefficient if true else zero`(hasSteroidsUsage: Boolean, expected: BigDecimal) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getSteroidsUsageWeight(hasSteroidsUsage),
    )
  }

  @ParameterizedTest
  @MethodSource("getOtherDrugsUsageWeightProvider")
  fun `getOtherDrugsUsageWeight returns coefficient if any parameters are true else zero`(
    hasOtherDrugsUsage: Boolean,
    hasKetamineUsage: Boolean,
    hasSpiceUsage: Boolean,
    hasHallucinogensUsage: Boolean,
    hasSolventsUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight(
        hasOtherDrugsUsage,
        hasKetamineUsage,
        hasSpiceUsage,
        hasHallucinogensUsage,
        hasSolventsUsage,
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("getRiskBandProvider")
  fun `getRiskBand returns correct band mapping based on boundaries`(percentageScore: Double, expected: RiskBand) {
    assertEquals(
      expected,
      AllReoffendingPredictorTransformationHelper.getRiskBand(percentageScore),
    )
  }

  @ParameterizedTest
  @MethodSource("getRiskBandOutOfBoundsProvider")
  fun `getRiskBand throws exception when percentage score is outside of upper and lower bounds`(percentageScore: Double) {
    assertThrows<IllegalArgumentException> {
      AllReoffendingPredictorTransformationHelper.getRiskBand(percentageScore)
    }
  }

  companion object {
    @JvmStatic
    fun get2YearInterceptWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, BigDecimal(5.01702292499072)),
      Arguments.of(StaticOrDynamic.DYNAMIC, BigDecimal(3.83654148692014)),
    )

    @JvmStatic
    fun getAgeGenderPolynomialWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        30,
        calculatePolynomial(
          arrayOf(
            BigDecimal(-0.142428460338541),
            BigDecimal(0.0011000413899151),
            BigDecimal(0.0000198538471606),
            BigDecimal(-0.0000002648918335),
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
            BigDecimal(-0.110202179628585),
            BigDecimal(0.0006726723443858),
            BigDecimal(0.0000138719445957),
            BigDecimal(-0.0000001610775422),
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
            BigDecimal(-0.0175014516689226),
            BigDecimal(0.0001625346907234),
            BigDecimal(0.0000003645241305),
            BigDecimal(-0.0000000746220588),
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
            BigDecimal(-0.0097677485919972),
            BigDecimal(0.000126702732348),
            BigDecimal(-0.0000006094926795),
            BigDecimal(-0.0000000713415862),
          ),
          30.toBigDecimal(),
        ),
      ),
    )

    @JvmStatic
    fun getGenderWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, Gender.MALE, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, Gender.FEMALE, BigDecimal(-2.95224200717183)),
      Arguments.of(StaticOrDynamic.DYNAMIC, Gender.FEMALE, BigDecimal(-2.68801056322021)),
    )

    @JvmStatic
    fun getFirstSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 3, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 1, BigDecimal(-3.94357049933093)),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, BigDecimal(-3.39824378336932)),
    )

    @JvmStatic
    fun getSecondSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 2, BigDecimal(-3.06189212045904)),
      Arguments.of(StaticOrDynamic.DYNAMIC, 2, BigDecimal(-2.60344581288004)),
    )

    @JvmStatic
    fun getTotalSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, BigDecimal(-0.0042757301284995)),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, BigDecimal(-0.0030262875646805)),
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
        BigDecimal(-0.0432051839161834),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        2,
        20,
        24,
        BigDecimal(-0.1278900114848396),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        2,
        19,
        24,
        BigDecimal(-0.2139991579208035),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        2,
        21,
        23,
        BigDecimal(-0.0622824723468828),
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
            BigDecimal(-0.0762086223169624),
            BigDecimal(0.0016230134182902),
            BigDecimal(0.0000224473135387),
            BigDecimal(-0.0000012808638685),
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
            BigDecimal(-0.0531180383905312),
            BigDecimal(0.0004075218530422),
          ),
          12.toBigDecimal(),
        ),
      ),
    )

    @JvmStatic
    fun getCopasWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        18,
        20,
        30,
        BigDecimal("-1.0764458092822229259535105313016156713956661405973136425018310546875"),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        7,
        24,
        26,
        BigDecimal("-1.4137472471154750807994307667240896364546642871573567390441894531250"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        5,
        18,
        42,
        BigDecimal("-2.867651518363984182707421540996062248041198472492396831512451171875"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        6,
        21,
        25,
        BigDecimal("-1.335755183161743137999572325975761177829781445325352251529693603515625"),
      ),
    )

    @JvmStatic
    fun getCopasSquaredWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        18,
        20,
        30,
        BigDecimal("0.0621393752987599374032687335080660240294037066632881760597229003906250"),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        7,
        24,
        26,
        BigDecimal("-0.0757947243395682004695621971231023206172494610655121505260467529296875000"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        5,
        18,
        42,
        BigDecimal("0.42515616770311838428496597282411906260080058927997015416622161865234375"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        6,
        21,
        25,
        BigDecimal("-0.1113871778541978141098613095013716844938755912153283134102821350097656250"),
      ),
    )

    @JvmStatic
    fun getUnemployedWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.0317783733809377)),
    )

    @JvmStatic
    fun getLiveInRelationshipWeightProvider() = listOf(
      Arguments.of(CurrentRelationshipStatus.NOT_IN_RELATIONSHIP, BigDecimal.ZERO),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        BigDecimal(-0.266331545877519),
      ),
    )

    @JvmStatic
    fun getMultiplicativeRelationshipWeightProvider() = listOf(
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SOME_PROBLEMS,
        BigDecimal(0.141655261924428),
      ),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SIGNIFICANT_PROBLEMS,
        BigDecimal(0.283310523848856),
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
      Arguments.of(true, BigDecimal(0.0635669196833949)),
    )

    @JvmStatic
    fun getHeroinUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.182096496867273)),
    )

    @JvmStatic
    fun getOtherOpiateUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.17892958261215)),
    )

    @JvmStatic
    fun getCrackCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.109095964190426)),
    )

    @JvmStatic
    fun getPowderCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.0605135470691152)),
    )

    @JvmStatic
    fun getMisusedPrescriptionDrugUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.0252240614607483)),
    )

    @JvmStatic
    fun getBenzodiazepinesUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.0764353348164513)),
    )

    @JvmStatic
    fun getCannabisUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.049300440360878)),
    )

    @JvmStatic
    fun getSteroidsUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, BigDecimal(0.202231737251706)),
    )

    @JvmStatic
    fun getOtherDrugsUsageWeightProvider() = listOf(
      Arguments.of(false, false, false, false, false, BigDecimal.ZERO),
      Arguments.of(true, false, false, false, false, BigDecimal(0.0267794308651123)),
      Arguments.of(false, true, false, false, false, BigDecimal(0.0267794308651123)),
      Arguments.of(false, false, true, false, false, BigDecimal(0.0267794308651123)),
      Arguments.of(false, false, false, true, false, BigDecimal(0.0267794308651123)),
      Arguments.of(false, false, false, false, true, BigDecimal(0.0267794308651123)),
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
