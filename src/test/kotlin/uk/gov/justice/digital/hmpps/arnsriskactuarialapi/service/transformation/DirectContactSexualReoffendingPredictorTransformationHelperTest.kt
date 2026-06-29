package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getAgeAtLastSanctionForSexualOffenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getAgeAtStartOfFollowupWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getIsCurrentOffenceAgainstVictimStrangerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getRiskBandReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getRiskReduction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalContactAdultSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalContactChildSexualSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalNonContactSexualOffencesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.DirectContactSexualReoffendingPredictorTransformationHelper.getTotalNumberOfSanctionsForAllOffencesWeight
import java.time.LocalDate
import java.util.stream.Stream

class DirectContactSexualReoffendingPredictorTransformationHelperTest {

  @ParameterizedTest
  @MethodSource("getTotalContactAdultSexualSanctionsWeightProvider")
  fun `test getTotalContactAdultSexualSanctionsWeight with valid inputs`(input: Int, expected: Int) {
    val result = getTotalContactAdultSexualSanctionsWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalContactChildSexualSanctionsWeightProvider")
  fun `test getTotalContactChildSexualSanctionsWeight with valid inputs`(input: Int, expected: Int) {
    val result = getTotalContactChildSexualSanctionsWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalNonContactSexualOffencesExcludingIndecentImagesWeightProvider")
  fun `test getTotalNonContactSexualOffencesExcludingIndecentImagesWeight with valid inputs`(
    getTotalNonContactSexualOffencesExcludingIndecentImagesWeight: Int,
    expected: Int,
  ) {
    val result =
      getTotalNonContactSexualOffencesWeight(getTotalNonContactSexualOffencesExcludingIndecentImagesWeight)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeAtStartOfFollowupWeightProvider")
  fun `getAgeAtStartOfFollowupWeight test valid ages return correct weights`(age: Int, expected: Int) {
    val result = getAgeAtStartOfFollowupWeight(age)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getAgeAtLastSanctionForSexualOffenceWeightProvider")
  fun `test valid ages return correct weights`(age: Int, expectedWeight: Int) {
    val result = getAgeAtLastSanctionForSexualOffenceWeight(age)
    assertEquals(expectedWeight, result)
  }

  @ParameterizedTest
  @MethodSource("getTotalNumberOfSanctionsWeightProvider")
  fun `test valid totalNumberOfSanctionsForAllOffences returns correct weight`(input: Int, expected: Int) {
    val result = getTotalNumberOfSanctionsForAllOffencesWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getStrangerVictimProvider")
  fun `test all branches of getStrangerVictimWeight`(input: Boolean?, expected: Int) {
    val result = getIsCurrentOffenceAgainstVictimStrangerWeight(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getBandProvider")
  fun `test valid  scores return correct RiskBand`(score: Int, expected: RiskBand) {
    val result = getBand(score)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("getScoreProvider")
  fun `test  score calculation for given input`(input: Int, expected: Double) {
    val result = getScore(input)
    assertEquals(expected, result)
  }

  @ParameterizedTest
  @MethodSource("riskReductionProvider")
  fun `getRiskReduction should correctly determine eligibility`(
    gender: Gender,
    supervisionStatus: SupervisionStatus,
    mostRecentOffenceDate: LocalDate?,
    dateOfMostRecentSexualOffence: LocalDate?,
    dateAtStartOfFollowup: LocalDate,
    assessmentDate: LocalDate,
    riskBand: RiskBand?,
    expectedResult: Boolean,
  ) {
    val actualResult = getRiskReduction(
      gender = gender,
      supervisionStatus = supervisionStatus,
      mostRecentOffenceDate = mostRecentOffenceDate,
      dateOfMostRecentSexualOffence = dateOfMostRecentSexualOffence,
      dateAtStartOfFollowup = dateAtStartOfFollowup,
      assessmentDate = assessmentDate,
      riskBand = riskBand,
    )

    assertEquals(expectedResult, actualResult)
  }

  @ParameterizedTest
  @MethodSource("riskBandReductionProvider")
  fun `getRiskBandReduction should correctly reduce risk band`(
    ospRiskReduction: Boolean,
    riskBand: RiskBand,
    expectedResult: RiskBand,
  ) {
    val actualResult = getRiskBandReduction(ospRiskReduction, riskBand)
    assertEquals(expectedResult, actualResult)
  }

  companion object {
    @JvmStatic
    fun getTotalContactAdultSexualSanctionsWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0),
      Arguments.of(1, 5),
      Arguments.of(2, 10),
      Arguments.of(3, 15),
      Arguments.of(Int.MAX_VALUE, 15),
    )

    @JvmStatic
    fun getTotalContactChildSexualSanctionsWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0),
      Arguments.of(1, 3),
      Arguments.of(2, 6),
      Arguments.of(3, 9),
      Arguments.of(Int.MAX_VALUE, 9),
    )

    @JvmStatic
    fun getTotalNonContactSexualOffencesExcludingIndecentImagesWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0),
      Arguments.of(1, 2),
      Arguments.of(2, 4),
      Arguments.of(3, 6),
      Arguments.of(Int.MAX_VALUE, 6),
    )

    @JvmStatic
    fun getAgeAtStartOfFollowupWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, 14),
      Arguments.of(20, 14),
      Arguments.of(21, 13),
      Arguments.of(24, 12),
      Arguments.of(27, 11),
      Arguments.of(30, 10),
      Arguments.of(33, 9),
      Arguments.of(36, 8),
      Arguments.of(39, 7),
      Arguments.of(42, 6),
      Arguments.of(45, 5),
      Arguments.of(48, 4),
      Arguments.of(51, 3),
      Arguments.of(54, 2),
      Arguments.of(57, 1),
      Arguments.of(60, 0),
      Arguments.of(150, 0), // Ensure upper bound coverage
    )

    @JvmStatic
    fun getAgeAtLastSanctionForSexualOffenceWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(10, 0),
      Arguments.of(15, 0),
      Arguments.of(16, 5),
      Arguments.of(17, 5),
      Arguments.of(18, 10),
      Arguments.of(35, 10),
      Arguments.of(100, 10),
    )

    @JvmStatic
    fun getTotalNumberOfSanctionsWeightProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(1, 0), // exact match
      Arguments.of(2, 6), // in 2..Int.MAX_VALUE
      Arguments.of(5, 6),
      Arguments.of(100, 6),
      Arguments.of(Int.MAX_VALUE, 6),
    )

    @JvmStatic
    fun getStrangerVictimProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(true, 4),
      Arguments.of(false, 0),
      Arguments.of(null, 0),
    )

    @JvmStatic
    fun getBandProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, RiskBand.LOW),
      Arguments.of(1, RiskBand.LOW),
      Arguments.of(21, RiskBand.LOW),
      Arguments.of(22, RiskBand.MEDIUM),
      Arguments.of(29, RiskBand.MEDIUM),
      Arguments.of(30, RiskBand.HIGH),
      Arguments.of(35, RiskBand.HIGH),
      Arguments.of(36, RiskBand.VERY_HIGH),
      Arguments.of(64, RiskBand.VERY_HIGH),
    )

    @JvmStatic
    fun getScoreProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(0, 0.0),
      Arguments.of(32, 0.028765253876307774),
      Arguments.of(64, 0.8312061933744967),
    )

    private val ASSESSMENT_DATE = LocalDate.of(2026, 6, 16)
    private val MORE_THAN_5_YEARS_AGO = ASSESSMENT_DATE.minusYears(6)
    private val LESS_THAN_5_YEARS_AGO = ASSESSMENT_DATE.minusYears(4)

    @JvmStatic
    fun riskReductionProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        true,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        null,
        null,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        true,
      ),

      Arguments.of(
        Gender.FEMALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.CUSTODY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        null,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.LOW,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.NOT_APPLICABLE,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        LESS_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        LESS_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        false,
      ),
      Arguments.of(
        Gender.MALE,
        SupervisionStatus.COMMUNITY,
        MORE_THAN_5_YEARS_AGO,
        MORE_THAN_5_YEARS_AGO,
        LESS_THAN_5_YEARS_AGO,
        ASSESSMENT_DATE,
        RiskBand.HIGH,
        false,
      ),
    )

    @JvmStatic
    fun riskBandReductionProvider(): Stream<Arguments> = Stream.of(
      Arguments.of(false, RiskBand.VERY_HIGH, RiskBand.VERY_HIGH),
      Arguments.of(false, RiskBand.HIGH, RiskBand.HIGH),
      Arguments.of(false, RiskBand.MEDIUM, RiskBand.MEDIUM),
      Arguments.of(false, RiskBand.LOW, RiskBand.LOW),
      Arguments.of(false, RiskBand.NOT_APPLICABLE, RiskBand.NOT_APPLICABLE),
      Arguments.of(true, RiskBand.VERY_HIGH, RiskBand.HIGH),
      Arguments.of(true, RiskBand.HIGH, RiskBand.MEDIUM),
      Arguments.of(true, RiskBand.MEDIUM, RiskBand.LOW),
      Arguments.of(true, RiskBand.LOW, RiskBand.LOW),
      Arguments.of(true, RiskBand.NOT_APPLICABLE, RiskBand.NOT_APPLICABLE),
    )
  }
}
