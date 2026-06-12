package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.AllReoffendingPredictorDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients.AllReoffendingPredictorStatic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.constants.AllReoffendingPredictor
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.calculatePolynomial
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.arrayOf
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
    assertEquals(expected, result)
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
    expectedCoefficient: BigDecimal,
  ) {
    val totalSanctions = 5
    val ageAtFirst = 20
    val ageAtCurrent = 30

    val expectedLengthOfCareer = (ageAtCurrent - ageAtFirst) + AllReoffendingPredictor.CAREER_BOOST
    val expectedRatio = totalSanctions.toDouble() / expectedLengthOfCareer
    val expectedNaturalLog = ln(expectedRatio)
    val expectedWeight = expectedNaturalLog.toBigDecimal() * expectedCoefficient

    val actualWeight = AllReoffendingPredictorTransformationHelper.getCopasWeight(
      staticOrDynamic,
      totalSanctions,
      gender,
      ageAtFirst,
      ageAtCurrent,
    )

    assertEquals(expectedWeight, actualWeight)
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
    expectedCoefficient: BigDecimal,
  ) {
    val totalSanctions = 5
    val ageAtFirst = 20
    val ageAtCurrent = 30

    val expectedLengthOfCareer = (ageAtCurrent - ageAtFirst) + AllReoffendingPredictor.CAREER_BOOST
    val expectedRatio = totalSanctions.toDouble() / expectedLengthOfCareer
    val expectedNaturalLog = ln(expectedRatio)
    val expectedWeight = expectedNaturalLog.pow(2).toBigDecimal() * expectedCoefficient

    val actualWeight = AllReoffendingPredictorTransformationHelper.getCopasSquaredWeight(
      staticOrDynamic,
      totalSanctions,
      gender,
      ageAtFirst,
      ageAtCurrent,
    )

    assertEquals(expectedWeight, actualWeight)
  }

  @Test
  fun `getSuitableAccommodationWeight returns correct calculated weight`() {
    val problemLevel = ProblemLevel.SIGNIFICANT_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.ACCOMMODATION_SUITABILITY.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight(problemLevel)
    assertEquals(expected, result)
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
    val problemLevel = ProblemLevel.SIGNIFICANT_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.RELATIONSHIP_QUALITY.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getRelationshipQualityWeight(problemLevel)
    assertEquals(expected, result)
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
    assertEquals(expected, result)
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
    val problemLevel = ProblemLevel.SOME_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.ACTIVITIES_ENCOURAGE_OFFENDING.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getRegularOffendingActivitiesWeight(problemLevel)
    assertEquals(expected, result)
  }

  @Test
  fun `getDrugMotivationWeight returns correct calculated weight`() {
    val motivationLevel = MotivationLevel.PARTIAL_MOTIVATION
    val expected =
      motivationLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.MOTIVATION_TO_TACKLE_DRUG_MISUSE.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getDrugMotivationWeight(motivationLevel)
    assertEquals(expected, result)
  }

  @Test
  fun `getChronicDrinkingWeight returns correct calculated weight`() {
    val problemLevel = ProblemLevel.SOME_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.CHRONIC_DRINKING.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getChronicDrinkingWeight(problemLevel)
    assertEquals(expected, result)
  }

  @Test
  fun `getBingeDrinkingWeight returns correct calculated weight`() {
    val problemLevel = ProblemLevel.SIGNIFICANT_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.BINGE_DRINKING.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getBingeDrinkingWeight(problemLevel)
    assertEquals(expected, result)
  }

  @Test
  fun `getImpulsivityWeight returns correct calculated weight`() {
    val problemLevel = ProblemLevel.SOME_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.IMPULSIVITY.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getImpulsivityWeight(problemLevel)
    assertEquals(expected, result)
  }

  @Test
  fun `getProCriminalAttitudeWeight returns correct calculated weight`() {
    val problemLevel = ProblemLevel.SIGNIFICANT_PROBLEMS
    val expected =
      problemLevel.score.toBigDecimal() * AllReoffendingPredictorDynamic.PRO_CRIMINAL_ATTITUDE.coefficient

    val result = AllReoffendingPredictorTransformationHelper.getProCriminalAttitudeWeight(problemLevel)
    assertEquals(expected, result)
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
      Arguments.of(StaticOrDynamic.STATIC, AllReoffendingPredictorStatic.TWO_YEAR_CONSTANT.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, AllReoffendingPredictorDynamic.TWO_YEAR_CONSTANT.coefficient),
    )

    @JvmStatic
    fun getAgeGenderPolynomialWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        30,
        calculatePolynomial(
          arrayOf(
            BigDecimal.ZERO,
            AllReoffendingPredictorStatic.AAI_MALE.coefficient,
            AllReoffendingPredictorStatic.AAI_QUADRATIC_MALE.coefficient,
            AllReoffendingPredictorStatic.AAI_CUBIC_MALE.coefficient,
            AllReoffendingPredictorStatic.AAI_QUARTIC_MALE.coefficient,
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
            BigDecimal.ZERO,
            AllReoffendingPredictorDynamic.AAI_MALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_QUADRATIC_MALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_CUBIC_MALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_QUARTIC_MALE.coefficient,
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
            BigDecimal.ZERO,
            AllReoffendingPredictorStatic.AAI_FEMALE.coefficient,
            AllReoffendingPredictorStatic.AAI_QUADRATIC_FEMALE.coefficient,
            AllReoffendingPredictorStatic.AAI_CUBIC_FEMALE.coefficient,
            AllReoffendingPredictorStatic.AAI_QUARTIC_FEMALE.coefficient,
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
            BigDecimal.ZERO,
            AllReoffendingPredictorDynamic.AAI_FEMALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_QUADRATIC_FEMALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_CUBIC_FEMALE.coefficient,
            AllReoffendingPredictorDynamic.AAI_QUARTIC_FEMALE.coefficient,
          ),
          30.toBigDecimal(),
        ),
      ),
    )

    @JvmStatic
    fun getGenderWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, Gender.MALE, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, Gender.FEMALE, AllReoffendingPredictorStatic.FEMALE.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, Gender.FEMALE, AllReoffendingPredictorDynamic.FEMALE.coefficient),
    )

    @JvmStatic
    fun getFirstSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 3, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 1, AllReoffendingPredictorStatic.FIRST_SANCTION.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, AllReoffendingPredictorDynamic.FIRST_SANCTION.coefficient),
    )

    @JvmStatic
    fun getSecondSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, BigDecimal.ZERO),
      Arguments.of(StaticOrDynamic.STATIC, 2, AllReoffendingPredictorStatic.SECOND_SANCTION.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 2, AllReoffendingPredictorDynamic.SECOND_SANCTION.coefficient),
    )

    @JvmStatic
    fun getTotalSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, 1, AllReoffendingPredictorStatic.SANCTION_OCCASIONS.coefficient),
      Arguments.of(StaticOrDynamic.DYNAMIC, 1, AllReoffendingPredictorDynamic.SANCTION_OCCASIONS.coefficient),
    )

    @JvmStatic
    fun getGapBetweenFirstAndSecondSanctionWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, Gender.MALE, 10, 18, 24, BigDecimal.ZERO),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        2,
        18,
        24,
        AllReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient * 6.toBigDecimal(),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        2,
        18,
        24,
        AllReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE.coefficient * 6.toBigDecimal(),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        2,
        18,
        24,
        AllReoffendingPredictorStatic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient * 6.toBigDecimal(),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        2,
        18,
        24,
        AllReoffendingPredictorDynamic.YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE.coefficient * 6.toBigDecimal(),
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
            BigDecimal.ZERO,
            AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS.coefficient,
            AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
            AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_CUBIC.coefficient,
            AllReoffendingPredictorStatic.OFFENCE_FREE_MONTHS_QUARTIC.coefficient,
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
            BigDecimal.ZERO,
            AllReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS.coefficient,
            AllReoffendingPredictorDynamic.OFFENCE_FREE_MONTHS_QUADRATIC.coefficient,
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
        AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_G_MALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_G_FEMALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_G_MALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_G_FEMALE.coefficient,
      ),
    )

    @JvmStatic
    fun getCopasSquaredWeightProvider() = listOf(
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.MALE,
        AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        AllReoffendingPredictorStatic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE.coefficient,
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        AllReoffendingPredictorDynamic.THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE.coefficient,
      ),
    )

    @JvmStatic
    fun getUnemployedWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.UNEMPLOYED.coefficient),
    )

    @JvmStatic
    fun getLiveInRelationshipWeightProvider() = listOf(
      Arguments.of(CurrentRelationshipStatus.NOT_IN_RELATIONSHIP, BigDecimal.ZERO),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        AllReoffendingPredictorDynamic.LIVE_IN_RELATIONSHIP.coefficient,
      ),
    )

    @JvmStatic
    fun getMultiplicativeRelationshipWeightProvider() = listOf(
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SOME_PROBLEMS,
        ProblemLevel.SOME_PROBLEMS.score.toBigDecimal() * AllReoffendingPredictorDynamic.QUALITY_OF_LIVE_IN_RELATIONSHIP.coefficient,
      ),
      Arguments.of(
        CurrentRelationshipStatus.IN_RELATIONSHIP_LIVING_TOGETHER,
        ProblemLevel.SIGNIFICANT_PROBLEMS,
        ProblemLevel.SIGNIFICANT_PROBLEMS.score.toBigDecimal() * AllReoffendingPredictorDynamic.QUALITY_OF_LIVE_IN_RELATIONSHIP.coefficient,
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
      Arguments.of(true, AllReoffendingPredictorDynamic.DOMESTIC_ABUSE.coefficient),
    )

    @JvmStatic
    fun getHeroinUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.HEROIN.coefficient),
    )

    @JvmStatic
    fun getOtherOpiateUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.OTHER_OPIATE.coefficient),
    )

    @JvmStatic
    fun getCrackCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.CRACK_COCAINE.coefficient),
    )

    @JvmStatic
    fun getPowderCocaineUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.POWDER_COCAINE.coefficient),
    )

    @JvmStatic
    fun getMisusedPrescriptionDrugUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.PRESCRIPTION_DRUG_MISUSE.coefficient),
    )

    @JvmStatic
    fun getBenzodiazepinesUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.BENZODIAZEPINES.coefficient),
    )

    @JvmStatic
    fun getCannabisUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.CANNABIS.coefficient),
    )

    @JvmStatic
    fun getSteroidsUsageWeightProvider() = listOf(
      Arguments.of(false, BigDecimal.ZERO),
      Arguments.of(true, AllReoffendingPredictorDynamic.STEROIDS.coefficient),
    )

    @JvmStatic
    fun getOtherDrugsUsageWeightProvider() = listOf(
      Arguments.of(false, false, false, false, false, BigDecimal.ZERO),
      Arguments.of(true, false, false, false, false, AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient),
      Arguments.of(false, true, false, false, false, AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient),
      Arguments.of(false, false, true, false, false, AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient),
      Arguments.of(false, false, false, true, false, AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient),
      Arguments.of(false, false, false, false, true, AllReoffendingPredictorDynamic.OTHER_DRUGS.coefficient),
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
