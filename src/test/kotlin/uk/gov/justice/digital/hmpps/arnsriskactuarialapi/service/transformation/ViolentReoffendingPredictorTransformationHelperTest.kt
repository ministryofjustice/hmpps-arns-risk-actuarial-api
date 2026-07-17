package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeCacheService
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
  @MethodSource("getOffenceGroupWeightProvider")
  fun `getOffenceGroupWeight returns correct weight - happy path`(staticOrDynamic: StaticOrDynamic, category: ActuarialCategory, expectedWeight: BigDecimal) {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(category)
    assertEquals(
      expectedWeight,
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(offenceCodeCacheService, staticOrDynamic, offenceCode),
    )
  }

  @Test
  fun `getOffenceGroupWeight error case - no code mapping`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(null)
    val exception = assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.STATIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode not found, ensure this is validated before the calculation", exception.message)
  }

  @Test
  fun `getOffenceGroupWeight error case - UNKNOWN mapping (static)`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(ActuarialCategory.UNKNOWN)
    val exception = assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.STATIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode is UNKNOWN, ensure this is validated before the calculation", exception.message)
  }

  @Test
  fun `getOffenceGroupWeight error case - UNKNOWN mapping (dynamic)`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(ActuarialCategory.UNKNOWN)
    val exception = assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.DYNAMIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode is UNKNOWN, ensure this is validated before the calculation", exception.message)
  }

  @Test
  fun `getOffenceGroupWeight error case - NEED_DETAILS_OF_EXACT_OFFENCE mapping (static)`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE)
    val exception = assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.STATIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation", exception.message)
  }

  @Test
  fun `getOffenceGroupWeight error case - NEED_DETAILS_OF_EXACT_OFFENCE mapping (dynamic)`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE)
    val exception = assertThrows<IllegalArgumentException> {
      ViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.DYNAMIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation", exception.message)
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
    dateAtStartOfFollowup: LocalDate,
    expected: BigDecimal,
  ) {
    val result = ViolentReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight(
      staticOrDynamic,
      assessmentDate,
      dateAtStartOfFollowup,
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
      1,
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
    hasKetamineUsage: Boolean,
    hasSpiceUsage: Boolean,
    hasHallucinogensUsage: Boolean,
    hasSolventsUsage: Boolean,
    expected: BigDecimal,
  ) {
    assertEquals(
      expected,
      ViolentReoffendingPredictorTransformationHelper.getOtherDrugsUsageWeight(
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
        BigDecimal("-0.124535911906912169608439980608249886273597439867444336414337158203125"),
      ),
      Arguments.of(
        StaticOrDynamic.STATIC,
        Gender.FEMALE,
        7,
        24,
        26,
        BigDecimal("-0.352015821010451927053923269883950464276267666718922555446624755859375"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.MALE,
        5,
        18,
        42,
        BigDecimal("-1.182979102022626489455437082380484525856445543467998504638671875000000"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        Gender.FEMALE,
        6,
        21,
        25,
        BigDecimal("-0.4682952392033705726132414050685959239217481808736920356750488281250"),
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
        BigDecimal("-0.41703875913010355298627777416244910568821069318801164627075195312500"),
      ),
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        5,
        18,
        42,
        BigDecimal("-0.992361394166722204363525829734826722017260181019082665443420410156250"),
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
      Arguments.of(false, false, false, false, false, BigDecimal.ZERO),
      Arguments.of(true, false, false, false, false, BigDecimal(0.150053208890266)),
      Arguments.of(false, true, false, false, false, BigDecimal(0.150053208890266)),
      Arguments.of(false, false, true, false, false, BigDecimal(0.150053208890266)),
      Arguments.of(false, false, false, true, false, BigDecimal(0.150053208890266)),
      Arguments.of(false, false, false, false, true, BigDecimal(0.150053208890266)),
    )

    @JvmStatic
    fun getRiskBandProvider() = listOf(
      Arguments.of(0.01, RiskBand.LOW),
      Arguments.of(29.99, RiskBand.LOW),
      Arguments.of(30.00, RiskBand.MEDIUM),
      Arguments.of(59.99, RiskBand.MEDIUM),
      Arguments.of(60.00, RiskBand.HIGH),
      Arguments.of(79.99, RiskBand.HIGH),
      Arguments.of(80.00, RiskBand.VERY_HIGH),
      Arguments.of(99.99, RiskBand.VERY_HIGH),
    )

    @JvmStatic
    fun getRiskBandOutOfBoundsProvider() = listOf(
      Arguments.of(0.00),
      Arguments.of(100.00),
    )

    @JvmStatic
    fun getOffenceGroupWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.BURGLARY_DOMESTIC, BigDecimal("0.189290151743428")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.BURGLARY_OTHER, BigDecimal("0.152185842790808")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUNKENNESS, BigDecimal("0.897225111715064")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRINK_DRIVING, BigDecimal("-0.0955339743007365")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.MOTORING_OFFENCES, BigDecimal("-0.184914428055833")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VEHICLE_RELATED_THEFT, BigDecimal("0.165882021264931")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FRAUD_AND_FORGERY, BigDecimal("-0.362999252067375")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.WELFARE_FRAUD, BigDecimal("-0.987504454326342")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION, BigDecimal("-0.422720654391483")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY, BigDecimal("-0.0876488981693274")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS, BigDecimal("0.109809951520084")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH, BigDecimal("0.109809951520084")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT, BigDecimal("0.356342355096462")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.WEAPONS_NON_FIREARM, BigDecimal("0.109809951520084")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FIREARMS_MOST_SERIOUS, BigDecimal("0.109809951520084")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FIREARMS_OTHER, BigDecimal("0.109809951520084")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.HANDLING_STOLEN_GOODS, BigDecimal("0.0120019188396128")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.CRIMINAL_DAMAGE, BigDecimal("0.371503082545136")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.ACQUISITIVE_VIOLENCE, BigDecimal("-0.0032926198476618")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.OTHER_OFFENCES, BigDecimal("0.0998951874967628")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.ABSCONDING_OR_BAIL, BigDecimal("0.351192370764659")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.SEXUAL_AGAINST_CHILD, BigDecimal("-0.930849896127774")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, BigDecimal("-0.159028472471635")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.THEFT_NON_MOTOR, BigDecimal("0.260179775875471")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.BURGLARY_DOMESTIC, BigDecimal("0.235304808003462")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.BURGLARY_OTHER, BigDecimal("0.191809657473981")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUNKENNESS, BigDecimal("0.832786589840741")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRINK_DRIVING, BigDecimal("-0.0243380584056904")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.MOTORING_OFFENCES, BigDecimal("-0.0219990134288181")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VEHICLE_RELATED_THEFT, BigDecimal("0.200392887919135")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FRAUD_AND_FORGERY, BigDecimal("-0.215055195882386")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.WELFARE_FRAUD, BigDecimal("-0.937073522898054")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION, BigDecimal("-0.310067077915746")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY, BigDecimal("-0.0226462288920859")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS, BigDecimal("0.077028816727511")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH, BigDecimal("0.077028816727511")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT, BigDecimal("0.348649087179239")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.WEAPONS_NON_FIREARM, BigDecimal("0.077028816727511")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FIREARMS_MOST_SERIOUS, BigDecimal("0.077028816727511")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FIREARMS_OTHER, BigDecimal("0.077028816727511")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.HANDLING_STOLEN_GOODS, BigDecimal("0.160583400555203")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.CRIMINAL_DAMAGE, BigDecimal("0.302881895872264")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.ACQUISITIVE_VIOLENCE, BigDecimal("0.0442727884290873")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.OTHER_OFFENCES, BigDecimal("0.186300883246964")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.ABSCONDING_OR_BAIL, BigDecimal("0.332310036782829")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.SEXUAL_AGAINST_CHILD, BigDecimal("-0.994785930428512")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, BigDecimal("-0.249001167005262")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.THEFT_NON_MOTOR, BigDecimal("0.257081681109148")),
    )
  }
}
