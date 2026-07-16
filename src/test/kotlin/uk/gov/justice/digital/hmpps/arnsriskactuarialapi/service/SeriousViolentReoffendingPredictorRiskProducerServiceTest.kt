package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor.SeriousViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.SeriousViolentReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSeriousViolentReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validSeriousViolentReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class SeriousViolentReoffendingPredictorRiskProducerServiceTest {

  @Mock
  private lateinit var validator: SeriousViolentReoffendingPredictorValidator

  @Mock
  private lateinit var offenceCodeCacheService: OffenceCodeCacheService

  @InjectMocks
  private lateinit var service: SeriousViolentReoffendingPredictorRiskProducerService

  @Test
  fun `should return early with both static and dynamic errors when static validation fails`() {
    val request = RiskScoreRequest()

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

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(listOf(expectedStaticValidationErrors))
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationErrors))

    val context = service.getRiskScore(request, emptyContext())

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
    val request = validSeriousViolentReoffendingPredictorStaticRiskScoreRequest()

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

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationErrors))
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS)

    val context = service.getRiskScore(validSeriousViolentReoffendingPredictorStaticRiskScoreRequest(), emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("-0.97073455522398699457653492572717368602752685546875"),
      "ageGenderPolynomialWeight" to BigDecimal("-1.53995427562486324017665406671540040406398475170135498046875"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.0914232846880801"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.3616142884911799360025952410069294273853302001953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.0500925180037306017055698248441331088542938232421875"),
      "secondSanctionGapWeight" to BigDecimal("-1.8873049968465760439784162372234277427196502685546875"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "violentSanctionsWeight" to BigDecimal("0.0283895499511622011190592473894866998307406902313232421875"),
      "violenceRateWeight" to BigDecimal("-0.785373799886639244900096464390770734098623506724834442138671875"),
      "totalWeight" to BigDecimal("-6.475261599437733760220807512518348403318668715655803680419921875"),
    )

    val expected = SeriousViolentReoffendingPredictorObject(
      score = 0.15,
      band = RiskBand.LOW,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationErrors),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.seriousViolentReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val request = validSeriousViolentReoffendingPredictorDynamicRiskScoreRequest()

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS)

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("-1.705886969066070069089846583665348589420318603515625"),
      "ageGenderPolynomialWeight" to BigDecimal("-1.5049659847200444321747847542525278186076320707798004150390625"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("-0.048353695619906"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.17946726635963994311850910889916121959686279296875"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.051984151564695997571607932741244439966976642608642578125"),
      "secondSanctionGapWeight" to BigDecimal("-1.5482242049865344579728798635187558829784393310546875"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "violentSanctionsWeight" to BigDecimal("0.013065998449268000081158191960639669559895992279052734375"),
      "violenceRateWeight" to BigDecimal("-0.51778627936787994302401369086317484136117172965896315872669219970703125"),
      "offenceInvolveCarryingOrUsingWeaponsWeight" to BigDecimal("0.385215247009075001383138214805512689054012298583984375"),
      "suitableAccommodationWeight" to BigDecimal("0.044223953156797703167857349626501672901213169097900390625"),
      "unemployedWeight" to BigDecimal("0.14692569323665860547833972304943017661571502685546875"),
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
      "totalWeight" to BigDecimal("-4.88784017993249627601015451249486798435128775963676162064075469970703125"),
    )

    val expected = SeriousViolentReoffendingPredictorObject(
      score = 0.75,
      band = RiskBand.LOW,
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
      didOffenceInvolveCarryingOrUsingWeapon = false,
      suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
      isUnemployed = false,
      currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
      temperControl = ProblemLevel.NO_PROBLEMS,
      proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
      previousConvictions = listOf(),
    )

    // Mock out validation
    whenever(validator.validateStatic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(validator.validateDynamic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS)

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(
      BigDecimal("-1.407344044561080231175477506244675396374077536165714263916015625"),
      context.seriousViolentReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.308346403618800002650987730767884187343952362425625324249267578125"),
      context.seriousViolentReoffendingPredictor?.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName),
    )
  }
}
