package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSeriousViolentReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSeriousViolentReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal
import java.time.LocalDate

class SeriousViolentReoffendingPredictorRiskProducerServiceTest {

  private val service = SeriousViolentReoffendingPredictorRiskProducerService()

  @Test
  fun `should return early with both static and dynamic errors when static validation fails`() {
    val context = service.getRiskScore(RiskScoreRequest(), emptyContext())

    val expectedStaticValidationErrors = ValidationError(
      ValidationErrorType.MISSING_MANDATORY_INPUT,
      "Mandatory input field(s) missing",
      listOf(
        "dateOfBirth",
        "dateOfCurrentConviction",
        "ageAtFirstSanction",
        "gender",
        "currentOffenceCode",
        "totalNumberOfSanctionsForAllOffences",
        "totalNumberOfViolentSanctions",
      ),
    )
    val expectedDynamicValidationErrors = ValidationError(
      ValidationErrorType.MISSING_DYNAMIC_INPUT,
      "Dynamic input field(s) missing",
      listOf(
        "didOffenceInvolveCarryingOrUsingWeapon",
        "suitabilityOfAccommodation",
        "isUnemployed",
        "currentAlcoholUseProblems",
        "temperControl",
        "proCriminalAttitudes",
        "previousConvictions",
      ),
    )
    val expected = SeriousViolentReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationErrors, expectedDynamicValidationErrors),
      featureValues = null,
    )

    assertEquals(expected, context.seriousViolentReoffendingPredictor)
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val context = service.getRiskScore(validSeriousViolentReoffendingPredictorStaticRiskScoreRequest(), emptyContext())

    val expectedDynamicValidationErrors = ValidationError(
      ValidationErrorType.MISSING_DYNAMIC_INPUT,
      "Dynamic input field(s) missing",
      listOf(
        "didOffenceInvolveCarryingOrUsingWeapon",
        "suitabilityOfAccommodation",
        "isUnemployed",
        "currentAlcoholUseProblems",
        "temperControl",
        "proCriminalAttitudes",
        "previousConvictions",
      ),
    )
    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("-0.97073455522398699457653492572717368602752685546875"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.042776507656246201116018168519872233446221798658370971679687500"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.3616142884911799360025952410069294273853302001953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.05009251800373060170556982484413310885429382324218750"),
      "secondSanctionGapWeight" to BigDecimal("-1.88730499684657604397841623722342774271965026855468750000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "violentSanctionsWeight" to BigDecimal("0.02838954995116220111905924738948669983074069023132324218750"),
      "violenceRateWeight" to BigDecimal("-0.785373799886639244900096464390770734098623506724834442138671875"),
      "totalWeight" to BigDecimal("-5.069507116157196821160171614322820232700905762612819671630859375"),
    )

    val expected = SeriousViolentReoffendingPredictorObject(
      score = 0.62,
      band = RiskBand.LOW,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationErrors),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.seriousViolentReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val context = service.getRiskScore(validSeriousViolentReoffendingPredictorDynamicRiskScoreRequest(), emptyContext())

    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("-1.705886969066070069089846583665348589420318603515625"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.04180461068666790089374402095145910607243422418832778930664062500"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.17946726635963994311850910889916121959686279296875"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.0519841515646959975716079327412444399669766426086425781250"),
      "secondSanctionGapWeight" to BigDecimal("-1.54822420498653445797287986351875588297843933105468750000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "violentSanctionsWeight" to BigDecimal("0.0130659984492680000811581919606396695598959922790527343750"),
      "violenceRateWeight" to BigDecimal("-0.51778627936787994302401369086317484136117172965896315872669219970703125"),
      "offenceInvolveCarryingOrUsingWeaponsWeight" to BigDecimal("0.385215247009075001383138214805512689054012298583984375"),
      "suitableAccommodationWeight" to BigDecimal("0.044223953156797703167857349626501672901213169097900390625"),
      "unemployedWeight" to BigDecimal("0.073462846618329302739169861524715088307857513427734375"),
      "chronicDrinkingProblemsWeight" to BigDecimal("0.076554599290370506370351222358294762670993804931640625"),
      "temperControlWeight" to BigDecimal("0.12420740735737599569343814209787524305284023284912109375"),
      "proCriminalAttitudesWeight" to BigDecimal("0.0982315473681467932198785319997114129364490509033203125"),
      "pastHomicideOffenceWeight" to BigDecimal("0"),
      "pastWoundingGrievousBodilyHarmOffenceWeight" to BigDecimal("0"),
      "pastKidnappingOffenceWeight" to BigDecimal("0"),
      "pastFirearmsOffenceWeight" to BigDecimal("0.78040392588458196154732604554737918078899383544921875"),
      "pastRobberyOffenceWeight" to BigDecimal("0"),
      "pastAggravatedBurglaryOffenceWeight" to BigDecimal("0"),
      "pastNonFirearmWeaponOffenceWeight" to BigDecimal("0"),
      "pastCriminalDamageOffenceWeight" to BigDecimal("0"),
      "pastArsonOffenceWeight" to BigDecimal("0"),
      "totalWeight" to BigDecimal("-3.44978795689754304746828364071851436012394742647302336990833282470703125"),
    )

    val expected = SeriousViolentReoffendingPredictorObject(
      score = 3.08,
      band = RiskBand.HIGH,
      staticOrDynamic = StaticOrDynamic.DYNAMIC,
      validationErrors = emptyList(),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.seriousViolentReoffendingPredictor)
  }

  @Test
  fun `should fallback to dateOfCurrentConviction when dateAtStartOfFollowup is null`() {
    val requestMissingDateAtStartOfFollowup = RiskScoreRequest(
      assessmentDate = LocalDate.of(2025, 1, 1),
      dateOfBirth = LocalDate.of(1990, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
      ageAtFirstSanction = 18,
      gender = Gender.MALE,
      currentOffenceCode = "00001",
      totalNumberOfSanctionsForAllOffences = 2,
      totalNumberOfViolentSanctions = 2,
    )

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(
      BigDecimal("-0.042356215648992801134017915920537689089542254805564880371093750"),
      context.seriousViolentReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.028848120515418998943682677739419606410820051678456366062164306640625000000"),
      context.seriousViolentReoffendingPredictor?.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName),
    )
  }
}
