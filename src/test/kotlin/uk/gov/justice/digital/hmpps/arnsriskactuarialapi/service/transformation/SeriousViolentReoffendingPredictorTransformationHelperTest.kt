package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.OffenceCodeCacheService
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getChronicDrinkingWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getCopasWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getFirstSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getGapBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getNeverViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOffenceFreeMonthsPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOffenceInvolvedCarryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getOnceViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastAggravatedBurglaryOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastArsonOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastCriminalDamageOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastFirearmsOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastHomicideOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastKidnappingOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastNonFirearmWeaponOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastRobberyOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getPastWoundingGrievousBodilyHarmOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getProCriminalAttitudeWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getSuitableAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getTemperControlWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SeriousViolentReoffendingPredictorTransformationHelper.getViolentSanctionsWeight
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.util.stream.Stream

class SeriousViolentReoffendingPredictorTransformationHelperTest {

  @Test
  fun `getOffenceInvolvedCarryingOrUsingWeaponWeight should lookup value`() {
    assertEquals(
      BigDecimal("0.385215247009075001383138214805512689054012298583984375"),
      getOffenceInvolvedCarryingOrUsingWeaponWeight(true),
    )
    assertEquals(BigDecimal.ZERO, getOffenceInvolvedCarryingOrUsingWeaponWeight(false))
  }

  @Test
  fun `getUnemployedWeight should lookup value`() {
    assertEquals(BigDecimal("0.146925693236658605478339723049430176615715026855468750"), getUnemployedWeight(true))
    assertEquals(BigDecimal.ZERO, getUnemployedWeight(false))
  }

  @Test
  fun `getSuitableAccommodationWeight should lookup value`() {
    assertEquals(
      BigDecimal("0.088447906313595406335714699253003345802426338195800781250"),
      getSuitableAccommodationWeight(ProblemLevel.SIGNIFICANT_PROBLEMS),
    )
    assertEquals(
      BigDecimal("0.044223953156797703167857349626501672901213169097900390625"),
      getSuitableAccommodationWeight(ProblemLevel.SOME_PROBLEMS),
    )
    assertThat(getSuitableAccommodationWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `getChronicDrinkingWeight should lookup value`() {
    assertEquals(
      BigDecimal("0.153109198580741012740702444716589525341987609863281250"),
      getChronicDrinkingWeight(ProblemLevel.SIGNIFICANT_PROBLEMS),
    )
    assertEquals(
      BigDecimal("0.076554599290370506370351222358294762670993804931640625"),
      getChronicDrinkingWeight(ProblemLevel.SOME_PROBLEMS),
    )
    assertThat(getChronicDrinkingWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `getTemperControlWeight should lookup value`() {
    assertEquals(
      BigDecimal("0.24841481471475199138687628419575048610568046569824218750"),
      getTemperControlWeight(ProblemLevel.SIGNIFICANT_PROBLEMS),
    )
    assertEquals(
      BigDecimal("0.12420740735737599569343814209787524305284023284912109375"),
      getTemperControlWeight(ProblemLevel.SOME_PROBLEMS),
    )
    assertThat(getTemperControlWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `getProCriminalAttitudeWeight should lookup value`() {
    assertEquals(
      BigDecimal("0.1964630947362935864397570639994228258728981018066406250"),
      getProCriminalAttitudeWeight(ProblemLevel.SIGNIFICANT_PROBLEMS),
    )
    assertEquals(
      BigDecimal("0.0982315473681467932198785319997114129364490509033203125"),
      getProCriminalAttitudeWeight(ProblemLevel.SOME_PROBLEMS),
    )
    assertThat(getProCriminalAttitudeWeight(ProblemLevel.NO_PROBLEMS)).isEqualByComparingTo(BigDecimal.ZERO)
  }

  @Test
  fun `getPastHomicideOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastHomicideOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.355308819798017),
      getPastHomicideOffenceWeight(
        listOf(
          PreviousConviction.HOMICIDE,
        ),
      ),
    )
  }

  @Test
  fun `getPastWoundingGrievousBodilyHarmOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastWoundingGrievousBodilyHarmOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.399845826788494),
      getPastWoundingGrievousBodilyHarmOffenceWeight(
        listOf(
          PreviousConviction.WOUNDING_GBH,
        ),
      ),
    )
  }

  @Test
  fun `getPastKidnappingOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastKidnappingOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.534510912919277),
      getPastKidnappingOffenceWeight(
        listOf(
          PreviousConviction.KIDNAPPING,
        ),
      ),
    )
  }

  @Test
  fun `getPastFirearmsOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastFirearmsOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.780403925884582),
      getPastFirearmsOffenceWeight(
        listOf(
          PreviousConviction.FIREARMS,
        ),
      ),
    )
  }

  @Test
  fun `getPastRobberyOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastRobberyOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.290562230335504),
      getPastRobberyOffenceWeight(
        listOf(
          PreviousConviction.ROBBERY,
        ),
      ),
    )
  }

  @Test
  fun `getPastAggravatedBurglaryOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastAggravatedBurglaryOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.183891831607701),
      getPastAggravatedBurglaryOffenceWeight(
        listOf(
          PreviousConviction.AGGRAVATED_BURGLARY,
        ),
      ),
    )
  }

  @Test
  fun `getPastNonFirearmWeaponOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastNonFirearmWeaponOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.231407077551008),
      getPastNonFirearmWeaponOffenceWeight(
        listOf(
          PreviousConviction.WEAPON,
        ),
      ),
    )
  }

  @Test
  fun `getPastCriminalDamageOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastCriminalDamageOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(1.01491061134195),
      getPastCriminalDamageOffenceWeight(
        listOf(
          PreviousConviction.CRIMINAL_DAMAGE,
        ),
      ),
    )
  }

  @Test
  fun `getPastArsonOffenceWeight should return coefficient if applicable`() {
    assertEquals(BigDecimal.ZERO, getPastArsonOffenceWeight(emptyList()))
    assertEquals(
      BigDecimal(0.0073649024927637),
      getPastArsonOffenceWeight(
        listOf(
          PreviousConviction.ARSON,
        ),
      ),
    )
  }

  @ParameterizedTest
  @MethodSource("get2YearInterceptWeightProvider")
  fun `get2YearInterceptWeight should return valid coefficients`(
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = get2YearInterceptWeight(staticOrDynamic)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeGenderPolynomialWeightValidInputProvider")
  fun `getAgeGenderPolynomialWeight should calculate correct polynomial weight`(
    gender: Gender,
    ageAtStartOfFollowUp: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getAgeGenderPolynomialWeight(staticOrDynamic, gender, ageAtStartOfFollowUp)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getGenderWeightValidInputProvider")
  fun `getFemaleWeight should calculate correct polynomial weight`(
    gender: Gender,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getGenderWeight(staticOrDynamic, gender)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getFirstSanctionWeightInputProvider")
  fun `getFirstSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getFirstSanctionWeight(staticOrDynamic, totalNumberOfSanctionsForAllOffences)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getSecondSanctionWeightInputProvider")
  fun `getSecondSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getSecondSanctionWeight(staticOrDynamic, totalNumberOfSanctionsForAllOffences)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalSanctionWeightValidInputProvider")
  fun `getTotalSanctionWeight should return correct weight for valid sanction values`(
    totalNumberOfSanctionsForAllOffences: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getTotalSanctionWeight(staticOrDynamic, totalNumberOfSanctionsForAllOffences)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getGapBetweenFirstAndSecondSanctionWeightValidInputProvider")
  fun `getGapBetweenFirstAndSecondSanctionWeight should calculate correct weight for years between sanctions`(
    totalNumberOfSanctionsForAllOffences: Int,
    gender: Gender,
    ageAtCurrentSanction: Int,
    ageAtFirstSanction: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getGapBetweenFirstAndSecondSanctionWeight(
      staticOrDynamic,
      gender,
      ageAtFirstSanction,
      ageAtCurrentSanction,
      totalNumberOfSanctionsForAllOffences,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOffenceFreeMonthsPolynomialWeightValidInputProvider")
  fun `getOffenceFreeMonthsPolynomialWeight should return correct months since last sanction weight`(
    assessmentDate: LocalDate,
    dateAtStartOfFollowup: LocalDate,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getOffenceFreeMonthsPolynomialWeight(
      staticOrDynamic,
      assessmentDate,
      dateAtStartOfFollowup,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getCopasWeightValidInputProvider")
  fun `getCopasWeight should return correct weight for 3+ sanctions`(
    staticOrDynamic: StaticOrDynamic,
    totalSanctions: Int,
    gender: Gender,
    ageAtFirstSanction: Int,
    ageAtCurrentSanction: Int,
    expected: BigDecimal,
  ) {
    val result = getCopasWeight(
      staticOrDynamic,
      totalSanctions,
      gender,
      ageAtFirstSanction,
      ageAtCurrentSanction,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getNeverViolentHistoryWeightInputProvider")
  fun `getNeverViolentHistoryWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    gender: Gender,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getNeverViolentHistoryWeight(staticOrDynamic, totalViolentSanctions, gender)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getOnceViolentHistoryWeightInputProvider")
  fun `getOnceViolentHistoryWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getOnceViolentHistoryWeight(staticOrDynamic, totalViolentSanctions)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getViolentSanctionsWeightValidInputProvider")
  fun `getViolentSanctionsWeight should return correct violent sanctions weight`(
    totalViolentSanctions: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getViolentSanctionsWeight(staticOrDynamic, totalViolentSanctions)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getViolenceRateWeightValidInputProvider")
  fun `getViolenceRateWeight should return correct violence rate weight`(
    ageAtCurrentSanction: Int,
    ageAtFirstSanction: Int,
    totalNumberOfViolentSanctions: Int,
    staticOrDynamic: StaticOrDynamic,
    expected: BigDecimal,
  ) {
    val result = getViolenceRateWeight(
      staticOrDynamic,
      ageAtFirstSanction,
      totalNumberOfViolentSanctions,
      ageAtCurrentSanction,
    )
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getRiskBandProvider")
  fun `getRiskBand returns correct band mapping based on boundaries`(percentageScore: Double, expected: RiskBand) {
    assertEquals(
      expected,
      SeriousViolentReoffendingPredictorTransformationHelper.getRiskBand(percentageScore),
    )
  }

  @ParameterizedTest
  @MethodSource("getRiskBandOutOfBoundsProvider")
  fun `getRiskBand throws exception when percentage score is outside of upper and lower bounds`(percentageScore: Double) {
    assertThrows<IllegalArgumentException> {
      SeriousViolentReoffendingPredictorTransformationHelper.getRiskBand(percentageScore)
    }
  }

  @ParameterizedTest
  @MethodSource("getOffenceGroupWeightProvider")
  fun `getOffenceGroupWeight returns correct weight - happy path`(staticOrDynamic: StaticOrDynamic, category: ActuarialCategory, expectedWeight: BigDecimal) {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(category)
    assertEquals(
      expectedWeight,
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(offenceCodeCacheService, staticOrDynamic, offenceCode),
    )
  }

  @Test
  fun `getOffenceGroupWeight error case - no code mapping`() {
    val offenceCodeCacheService: OffenceCodeCacheService = mock()
    val offenceCode = "12345"

    whenever(offenceCodeCacheService.getActuarialCategory(offenceCode)).thenReturn(null)
    val exception = assertThrows<IllegalArgumentException> {
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
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
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
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
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
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
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
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
      SeriousViolentReoffendingPredictorTransformationHelper.getOffenceGroupWeight(
        offenceCodeCacheService,
        StaticOrDynamic.DYNAMIC,
        offenceCode,
      )
    }
    assertEquals("Offence code mapping for $offenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation", exception.message)
  }

  companion object {
    @JvmStatic
    fun get2YearInterceptWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, BigDecimal("-0.97073455522398699457653492572717368602752685546875")),
      Arguments.of(StaticOrDynamic.DYNAMIC, BigDecimal("-1.705886969066070069089846583665348589420318603515625")),
    )

    @JvmStatic
    fun getAgeGenderPolynomialWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Male, isSeriousViolentReoffendingPredictorDynamic = true
      Arguments.of(
        Gender.MALE,
        30,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.04056819432323010096328938511867079341755015775561332702636718750"),
      ),
      Arguments.of(
        Gender.MALE,
        31,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.04077426371713640095169849109080217886003083549439907073974609375"),
      ),
      // Male, isSeriousViolentReoffendingPredictorDynamic = false
      Arguments.of(
        Gender.MALE,
        30,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.041515631634486001170017410721868600376183167099952697753906250"),
      ),
      // Female, isSeriousViolentReoffendingPredictorDynamic = true
      Arguments.of(
        Gender.FEMALE,
        30,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.023211910414104098627141245514415857087442418560385704040527343750"),
      ),
      Arguments.of(
        Gender.FEMALE,
        31,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.023265867822882398626457671475531441274142707698047161102294921875"),
      ),
      // Female, isSeriousViolentReoffendingPredictorDynamic = false
      Arguments.of(
        Gender.FEMALE,
        30,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.0201549375772399013930684733009179865348414750769734382629394531250"),
      ),
    )

    @JvmStatic
    fun getGenderWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(Gender.MALE, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
      Arguments.of(
        Gender.FEMALE,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-1.609748656497000052212342779967002570629119873046875"),
      ),
      Arguments.of(
        Gender.FEMALE,
        StaticOrDynamic.STATIC,
        BigDecimal("-1.5585515193722498938910803190083242952823638916015625"),
      ),
    )

    @JvmStatic
    fun getFirstSanctionWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        1,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-2.26703484992351977922453443170525133609771728515625"),
      ),
      Arguments.of(
        1,
        StaticOrDynamic.STATIC,
        BigDecimal("-2.333549920169800007130334051907993853092193603515625"),
      ),
      Arguments.of(0, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
      Arguments.of(2, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getSecondSanctionWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        2,
        StaticOrDynamic.STATIC,
        BigDecimal("-1.3616142884911799360025952410069294273853302001953125"),
      ),
      Arguments.of(
        2,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-1.17946726635963994311850910889916121959686279296875"),
      ),
      Arguments.of(1, StaticOrDynamic.STATIC, BigDecimal.ZERO),
      Arguments.of(3, StaticOrDynamic.STATIC, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getTotalSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        1,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.0259920757823479987858039663706222199834883213043212890625"),
      ),
      Arguments.of(
        1,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.02504625900186530085278491242206655442714691162109375"),
      ),
      Arguments.of(
        2,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.0519841515646959975716079327412444399669766426086425781250"),
      ),
      Arguments.of(
        2,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.05009251800373060170556982484413310885429382324218750"),
      ),
    )

    @JvmStatic
    fun getGapBetweenFirstAndSecondSanctionWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Male, dynamic
      Arguments.of(
        2,
        Gender.MALE,
        30,
        25,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.48382006405829201811652495734961121343076229095458984375"),
      ),
      // Male, static
      Arguments.of(
        2,
        Gender.MALE,
        30,
        25,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.58978281151455501374325507413232116959989070892333984375"),
      ),
      // Female, dynamic
      Arguments.of(
        2,
        Gender.FEMALE,
        30,
        25,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.8564705097728785021526931586777209304273128509521484375"),
      ),
      // Female, static
      Arguments.of(
        2,
        Gender.FEMALE,
        30,
        25,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.201128035801098510593210022534549352712929248809814453125"),
      ),
      // number of sanctions not 2
      Arguments.of(1, Gender.FEMALE, 30, 25, StaticOrDynamic.STATIC, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getOffenceFreeMonthsPolynomialWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // dateAtStartOfFollowup > assessmentDate -> 0
      Arguments.of(
        LocalDate.of(2025, Month.JANUARY, 1),
        LocalDate.of(2025, Month.JANUARY, 2),
        StaticOrDynamic.STATIC,
        BigDecimal.ZERO,
      ),
      // Normal calculation for dynamic
      Arguments.of(
        LocalDate.of(2025, Month.JUNE, 1),
        LocalDate.of(2025, Month.JANUARY, 1),
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.02767046939861000016220832938809888468245645753995631821453571319580078125"),
      ),
      // Normal calculation for static
      Arguments.of(
        LocalDate.of(2025, Month.SEPTEMBER, 1),
        LocalDate.of(2025, Month.JANUARY, 1),
        StaticOrDynamic.STATIC,
        BigDecimal("-0.029902009080577798933136927928000403653641114942729473114013671875000000000"),
      ),
    )

    @JvmStatic
    fun getCopasWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Should return 0.0 for < 3 sanctions
      Arguments.of(StaticOrDynamic.DYNAMIC, 2, Gender.MALE, 18, 30, BigDecimal.ZERO),

      // Male - dynamic
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        5,
        Gender.MALE,
        18,
        30,
        BigDecimal("-1.34603134139388240263282725883868806704413145780563354492187500"),
      ),
      // Male - static
      Arguments.of(
        StaticOrDynamic.STATIC,
        6,
        Gender.MALE,
        20,
        30,
        BigDecimal("-1.217355696421490995930692185342258948566040999139659106731414794921875"),
      ),
      // Female - dynamic
      Arguments.of(
        StaticOrDynamic.DYNAMIC,
        4,
        Gender.FEMALE,
        17,
        30,
        BigDecimal("-1.554396467051176747796658669386826723268768546404317021369934082031250"),
      ),
      // Female - static
      Arguments.of(
        StaticOrDynamic.STATIC,
        4,
        Gender.FEMALE,
        17,
        30,
        BigDecimal("-1.564340305665617358688432855460548331905101804295554757118225097656250"),
      ),
    )

    @JvmStatic
    fun getNeverViolentHistoryWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        0,
        Gender.MALE,
        StaticOrDynamic.STATIC,
        BigDecimal("-1.5382238276355000294870478683151304721832275390625"),
      ),
      Arguments.of(
        0,
        Gender.FEMALE,
        StaticOrDynamic.STATIC,
        BigDecimal("-2.33948463970807996048506538500078022480010986328125"),
      ),
      Arguments.of(
        0,
        Gender.MALE,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-1.0987110077704300703516082649002783000469207763671875"),
      ),
      Arguments.of(
        0,
        Gender.FEMALE,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-1.367162912335520008610956210759468376636505126953125"),
      ),
      Arguments.of(1, Gender.MALE, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getOnceViolentHistoryWeightInputProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        1,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.00134885331124429998228553273520446964539587497711181640625"),
      ),
      Arguments.of(
        1,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("0.077746849480618995809066973379231058061122894287109375"),
      ),
      Arguments.of(0, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
    )

    @JvmStatic
    fun getViolentSanctionsWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Dynamic
      Arguments.of(0, StaticOrDynamic.DYNAMIC, BigDecimal.ZERO),
      Arguments.of(
        1,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("0.0065329992246340000405790959803198347799479961395263671875"),
      ),
      Arguments.of(
        2,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("0.0130659984492680000811581919606396695598959922790527343750"),
      ),

      // Static
      Arguments.of(0, StaticOrDynamic.STATIC, BigDecimal.ZERO),
      Arguments.of(
        1,
        StaticOrDynamic.STATIC,
        BigDecimal("0.01419477497558110055952962369474334991537034511566162109375"),
      ),
      Arguments.of(
        2,
        StaticOrDynamic.STATIC,
        BigDecimal("0.02838954995116220111905924738948669983074069023132324218750"),
      ),
    )

    @JvmStatic
    fun getViolenceRateWeightValidInputProvider(): Stream<Arguments> = Stream.of(
      // Static
      Arguments.of(
        40,
        18,
        6,
        StaticOrDynamic.STATIC,
        BigDecimal("-0.54090431489041145094615396831727593962568789720535278320312500"),
      ),
      // Dynamic
      Arguments.of(
        35,
        25,
        3,
        StaticOrDynamic.DYNAMIC,
        BigDecimal("-0.42774909019168334886759086074493330098533760974532924592494964599609375"),
      ),
    )

    @JvmStatic
    fun getRiskBandProvider() = listOf(
      Arguments.of(0.01, RiskBand.LOW),
      Arguments.of(0.99, RiskBand.LOW),
      Arguments.of(1.00, RiskBand.MEDIUM),
      Arguments.of(2.99, RiskBand.MEDIUM),
      Arguments.of(3.00, RiskBand.HIGH),
      Arguments.of(6.89, RiskBand.HIGH),
      Arguments.of(6.90, RiskBand.VERY_HIGH),
      Arguments.of(99.99, RiskBand.VERY_HIGH),
    )

    @JvmStatic
    fun getRiskBandOutOfBoundsProvider() = listOf(
      Arguments.of(0.00),
      Arguments.of(100.00),
    )

    @JvmStatic
    fun getOffenceGroupWeightProvider() = listOf(
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.BURGLARY_DOMESTIC, BigDecimal("0.427440297941078")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.BURGLARY_OTHER, BigDecimal("0.283654004619677")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUNKENNESS, BigDecimal("-0.169817158710549")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRINK_DRIVING, BigDecimal("-0.0870700781552191")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.MOTORING_OFFENCES, BigDecimal("0.0805237507490763")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VEHICLE_RELATED_THEFT, BigDecimal("0.286214108247545")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FRAUD_AND_FORGERY, BigDecimal("-0.0932918356073765")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.WELFARE_FRAUD, BigDecimal("-2.75700217295462")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION, BigDecimal("-0.151563625759297")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY, BigDecimal("0.196404663980207")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS, BigDecimal("0.0914232846880801")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH, BigDecimal("0.0881806276607277")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT, BigDecimal("-0.102680989477238")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.WEAPONS_NON_FIREARM, BigDecimal("0.4050968835329157")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FIREARMS_MOST_SERIOUS, BigDecimal("-0.0211330064692993")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.FIREARMS_OTHER, BigDecimal("0.3539581212971587")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.HANDLING_STOLEN_GOODS, BigDecimal("0.20156598193072")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.CRIMINAL_DAMAGE, BigDecimal("0.254391121973677")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.ACQUISITIVE_VIOLENCE, BigDecimal("0.34905826404493")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.OTHER_OFFENCES, BigDecimal("0.175156031546492")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.ABSCONDING_OR_BAIL, BigDecimal("0.361929924513904")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.SEXUAL_AGAINST_CHILD, BigDecimal("-0.269534814977957")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, BigDecimal("-0.481426313870417")),
      Arguments.of(StaticOrDynamic.STATIC, ActuarialCategory.THEFT_NON_MOTOR, BigDecimal("-0.126315026107379")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.BURGLARY_DOMESTIC, BigDecimal("0.392304322484302")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.BURGLARY_OTHER, BigDecimal("0.210475686624578")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUNKENNESS, BigDecimal("-0.266212126484843")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRINK_DRIVING, BigDecimal("0.0719681498702402")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.MOTORING_OFFENCES, BigDecimal("0.285285632874374")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VEHICLE_RELATED_THEFT, BigDecimal("0.335872011000959")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FRAUD_AND_FORGERY, BigDecimal("0.128223795291931")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.WELFARE_FRAUD, BigDecimal("-2.54378150425616")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION, BigDecimal("0.146018219011484")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY, BigDecimal("0.210481894371787")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS, BigDecimal("-0.048353695619906")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH, BigDecimal("0.110262124783647")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT, BigDecimal("-0.165010485104843")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.WEAPONS_NON_FIREARM, BigDecimal("0.0243282774787757")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FIREARMS_MOST_SERIOUS, BigDecimal("-0.382497575361466")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.FIREARMS_OTHER, BigDecimal("0.149093794035442")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.HANDLING_STOLEN_GOODS, BigDecimal("0.405987757430537")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.CRIMINAL_DAMAGE, BigDecimal("0.184857953812071")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.ACQUISITIVE_VIOLENCE, BigDecimal("0.0719174180811535")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.OTHER_OFFENCES, BigDecimal("0.135286411622716")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.ABSCONDING_OR_BAIL, BigDecimal("0.414243541008181")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.SEXUAL_AGAINST_CHILD, BigDecimal("-0.60993507861302")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD, BigDecimal("-0.545166046667834")),
      Arguments.of(StaticOrDynamic.DYNAMIC, ActuarialCategory.THEFT_NON_MOTOR, BigDecimal("-0.047968924813678")),
    )
  }
}
