package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.AllReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class AllReoffendingPredictorRiskProducerServiceTest {

  @Mock
  private lateinit var validator: AllReoffendingPredictorValidator

  @Mock
  private lateinit var offenceCodeCacheService: OffenceCodeCacheService

  @InjectMocks
  private lateinit var service: AllReoffendingPredictorRiskProducerService

  @Test
  fun `should return early with both static and dynamic errors when static and dynamic validation fails`() {
    val request = RiskScoreRequest()

    val staticValidationErrors = listOf(
      ValidationError(
        ValidationErrorType.MISSING_MANDATORY_INPUT,
        "Mandatory input field(s) missing",
        listOf(
          "dateOfBirth",
          "dateOfCurrentConviction",
          "ageAtFirstSanction",
          "gender",
          "currentOffenceCode",
          "totalNumberOfSanctionsForAllOffences",
        ),
      ),
      ValidationError(
        ValidationErrorType.DATE_OF_START_OF_FOLLOWUP_REQUIRED,
        "Either Date at start of followup or date of current conviction must be provided",
        listOf("dateAtStartOfFollowupCalculated"),
      ),
    )
    val dynamicValidationErrors = listOf(
      ValidationError(
        ValidationErrorType.MISSING_DYNAMIC_INPUT,
        "Dynamic input field(s) missing",
        listOf(
          "suitabilityOfAccommodation",
          "isUnemployed",
          "currentRelationshipWithPartner",
          "evidenceOfDomesticAbuse",
          "currentRelationshipStatus",
          "regularOffendingActivities",
          "motivationToTackleDrugMisuse",
          "hasHeroinUsage",
          "hasOtherOpiateUsage",
          "hasCrackCocaineUsage",
          "hasPowderCocaineUsage",
          "hasMisusedPrescriptionDrugUsage",
          "hasBenzodiazepinesUsage",
          "hasCannabisUsage",
          "hasSteroidsUsage",
          "hasOtherDrugsUsage",
          "hasKetamineUsage",
          "hasSpiceUsage",
          "hasHallucinogensUsage",
          "hasSolventsUsage",
          "currentAlcoholUseProblems",
          "excessiveAlcoholUse",
          "impulsivityProblems",
          "proCriminalAttitudes",
        ),
      ),
    )

    // Return errors from static and dynamic validation step
    whenever(validator.validateStatic(request)).thenReturn(staticValidationErrors)
    whenever(validator.validateDynamic(request)).thenReturn(dynamicValidationErrors)

    val context = service.getRiskScore(request, emptyContext())

    val expected = AllReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = staticValidationErrors + dynamicValidationErrors,
      featureValues = null,
    )

    assertEquals(expected, context.allReoffendingPredictor)
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val request = validAllReoffendingPredictorStaticRiskScoreRequest()

    val expectedDynamicValidationError = ValidationError(
      ValidationErrorType.MISSING_DYNAMIC_INPUT,
      "Dynamic input field(s) missing",
      listOf(
        "suitabilityOfAccommodation",
        "isUnemployed",
        "currentRelationshipWithPartner",
        "evidenceOfDomesticAbuse",
        "currentRelationshipStatus",
        "regularOffendingActivities",
        "motivationToTackleDrugMisuse",
        "hasHeroinUsage",
        "hasOtherOpiateUsage",
        "hasCrackCocaineUsage",
        "hasPowderCocaineUsage",
        "hasMisusedPrescriptionDrugUsage",
        "hasBenzodiazepinesUsage",
        "hasCannabisUsage",
        "hasSteroidsUsage",
        "hasOtherDrugsUsage",
        "hasKetamineUsage",
        "hasSpiceUsage",
        "hasHallucinogensUsage",
        "hasSolventsUsage",
        "currentAlcoholUseProblems",
        "excessiveAlcoholUse",
        "impulsivityProblems",
        "proCriminalAttitudes",
      ),
    )

    // Return errors from only dynamic validation step
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationError))
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.CRIMINAL_DAMAGE)

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("5.01702292499072033393758829333819448947906494140625"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.08945517776523581101263157648595392146262383903376758098602294921875000000"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.12167599899735"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-3.061892120459039912105936309671960771083831787109375"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.008551460256999000517086351180751080391928553581237792968750"),
      "secondSanctionGapWeight" to BigDecimal("-0.69128294265893441838954913691850379109382629394531250000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasScoreSquared" to BigDecimal("0"),
      "totalWeight" to BigDecimal("1.28751722284786119191238491908102492544685446773655712604522705078125000000"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 78.37,
      band = RiskBand.HIGH,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationError),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.allReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val request = validAllReoffendingPredictorDynamicRiskScoreRequest()

    // Return no errors from both static and dynamic validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.CRIMINAL_DAMAGE)

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("3.836541486920140187066863290965557098388671875"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.075523168843552202323133877397143276510860232519917190074920654296875000000"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.0250113803601321"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-2.603445812880039955672373253037221729755401611328125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.006052575129361000352834487614472891436889767646789550781250"),
      "secondSanctionGapWeight" to BigDecimal("-0.511560045939358443156663724948884919285774230957031250000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasScoreSquared" to BigDecimal("0"),
      "suitableAccommodationWeight" to BigDecimal("0.08480491583557529799985985619059647433459758758544921875"),
      "unemployedWeight" to BigDecimal("0.03177837338093769670166466312366537749767303466796875"),
      "liveInRelationshipWeight" to BigDecimal("0"),
      "relationshipQualityWeight" to BigDecimal("0.0364051885005138020634518625229247845709323883056640625"),
      "multiplicativeRelationshipWeight" to BigDecimal("0"),
      "domesticViolenceWeight" to BigDecimal("0"),
      "regularOffendingActivitiesWeight" to BigDecimal("0.1264916025920289899264759014840819872915744781494140625"),
      "drugMotivationWeight" to BigDecimal("0.086154249933930004967663762727170251309871673583984375"),
      "chronicDrinkingProblemsWeight" to BigDecimal("0.07450067036017719857010632722449372522532939910888671875"),
      "bingeDrinkingProblemsWeight" to BigDecimal("0.033842743646510797805859738218714483082294464111328125"),
      "impulsivityProblemsWeight" to BigDecimal("0.042198837009529699404453140232362784445285797119140625"),
      "proCriminalAttitudesWeight" to BigDecimal("0.043908309544141703451014535630747559480369091033935546875"),
      "heroinUsageWeight" to BigDecimal("0"),
      "otherOpiateUsageWeight" to BigDecimal("0"),
      "crackCocaineUsageWeight" to BigDecimal("0"),
      "powderCocaineUsageWeight" to BigDecimal("0"),
      "misusedPrescriptionDrugUsageWeight" to BigDecimal("0"),
      "benzodiazepinesUsageWeight" to BigDecimal("0"),
      "cannabisUsageWeight" to BigDecimal("0.049300440360878002021838284463228774257004261016845703125"),
      "steroidUsageWeight" to BigDecimal("0.202231737251706011893048753336188383400440216064453125"),
      "otherDrugUsageWeight" to BigDecimal("0"),
      "totalWeight" to BigDecimal("1.476588332903889890367294773122008866295118423295207321643829345703125000000"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 81.41,
      band = RiskBand.HIGH,
      staticOrDynamic = StaticOrDynamic.DYNAMIC,
      validationErrors = emptyList(),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.allReoffendingPredictor)
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
      suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
      isUnemployed = true,
      currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
      evidenceOfDomesticAbuse = false,
      currentRelationshipStatus = CurrentRelationshipStatus.NOT_IN_RELATIONSHIP,
      regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
      motivationToTackleDrugMisuse = MotivationLevel.PARTIAL_MOTIVATION,
      hasHeroinUsage = false,
      hasOtherOpiateUsage = false,
      hasCrackCocaineUsage = false,
      hasPowderCocaineUsage = false,
      hasMisusedPrescriptionDrugUsage = false,
      hasBenzodiazepinesUsage = false,
      hasCannabisUsage = false,
      hasSteroidsUsage = false,
      hasOtherDrugsUsage = false,
      hasKetamineUsage = false,
      hasSpiceUsage = false,
      hasHallucinogensUsage = false,
      hasSolventsUsage = false,
      currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
      excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
      impulsivityProblems = ProblemLevel.NO_PROBLEMS,
      proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
    )

    // Return no errors from both static and dynamic validation
    whenever(validator.validateStatic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(validator.validateDynamic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.CRIMINAL_DAMAGE)

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(
      BigDecimal("-0.077626343685467402225266887023057138517145858713774941861629486083984375000"),
      context.allReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.048227776154024796801539354262899905734229832887649536132812500"),
      context.allReoffendingPredictor?.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName),
    )
  }

  @Test
  fun `drug questions should fallback to 0 weighting when null`() {
    val requestMissingDateAtStartOfFollowup = RiskScoreRequest(
      assessmentDate = LocalDate.of(2025, 1, 1),
      dateOfBirth = LocalDate.of(1990, 1, 1),
      dateOfCurrentConviction = LocalDate.of(2024, 1, 1),
      ageAtFirstSanction = 18,
      gender = Gender.MALE,
      currentOffenceCode = "00001",
      totalNumberOfSanctionsForAllOffences = 2,
      suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
      isUnemployed = true,
      currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
      evidenceOfDomesticAbuse = false,
      currentRelationshipStatus = CurrentRelationshipStatus.NOT_IN_RELATIONSHIP,
      regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
      motivationToTackleDrugMisuse = null,
      hasHeroinUsage = null,
      hasOtherOpiateUsage = null,
      hasCrackCocaineUsage = null,
      hasPowderCocaineUsage = null,
      hasMisusedPrescriptionDrugUsage = null,
      hasBenzodiazepinesUsage = null,
      hasCannabisUsage = null,
      hasSteroidsUsage = null,
      hasOtherDrugsUsage = null,
      hasKetamineUsage = null,
      hasSpiceUsage = null,
      hasHallucinogensUsage = null,
      hasSolventsUsage = null,
      currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
      excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
      impulsivityProblems = ProblemLevel.NO_PROBLEMS,
      proCriminalAttitudes = ProblemLevel.NO_PROBLEMS,
    )

    // Return no errors from both static and dynamic validation
    whenever(validator.validateStatic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(validator.validateDynamic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.CRIMINAL_DAMAGE)

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("3.836541486920140187066863290965557098388671875"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.077626343685467402225266887023057138517145858713774941861629486083984375000"),
      "genderWeight" to BigDecimal.ZERO,
      "offenceGroupWeight" to BigDecimal("0.0250113803601321"),
      "firstSanctionWeight" to BigDecimal.ZERO,
      "secondSanctionWeight" to BigDecimal("-2.603445812880039955672373253037221729755401611328125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.006052575129361000352834487614472891436889767646789550781250"),
      "secondSanctionGapWeight" to BigDecimal("-0.511560045939358443156663724948884919285774230957031250000"),
      "offenceFreeMonthsWeight" to BigDecimal("-0.048227776154024796801539354262899905734229832887649536132812500"),
      "copasScore" to BigDecimal.ZERO,
      "copasScoreSquared" to BigDecimal.ZERO,
      "suitableAccommodationWeight" to BigDecimal("0E-56"),
      "unemployedWeight" to BigDecimal("0.03177837338093769670166466312366537749767303466796875"),
      "liveInRelationshipWeight" to BigDecimal.ZERO,
      "relationshipQualityWeight" to BigDecimal("0E-55"),
      "multiplicativeRelationshipWeight" to BigDecimal.ZERO,
      "domesticViolenceWeight" to BigDecimal.ZERO,
      "regularOffendingActivitiesWeight" to BigDecimal("0E-55"),
      "drugMotivationWeight" to BigDecimal("0E-54"),
      "chronicDrinkingProblemsWeight" to BigDecimal("0E-56"),
      "bingeDrinkingProblemsWeight" to BigDecimal("0E-54"),
      "impulsivityProblemsWeight" to BigDecimal("0E-54"),
      "proCriminalAttitudesWeight" to BigDecimal("0E-57"),
      "heroinUsageWeight" to BigDecimal.ZERO,
      "otherOpiateUsageWeight" to BigDecimal.ZERO,
      "crackCocaineUsageWeight" to BigDecimal.ZERO,
      "powderCocaineUsageWeight" to BigDecimal.ZERO,
      "misusedPrescriptionDrugUsageWeight" to BigDecimal.ZERO,
      "benzodiazepinesUsageWeight" to BigDecimal.ZERO,
      "cannabisUsageWeight" to BigDecimal.ZERO,
      "steroidUsageWeight" to BigDecimal.ZERO,
      "otherDrugUsageWeight" to BigDecimal.ZERO,
      "totalWeight" to BigDecimal("0.646418686872958385559850247202685891156903608134598471224308013916015625000"),
    )

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(expectedFeatureValues, context.allReoffendingPredictor?.featureValues)
  }
}
