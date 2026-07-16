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
        listOf("dateAtStartOfFollowup"),
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
      "ageGenderPolynomialWeight" to BigDecimal("-3.220386399548489196454736753494341172654458205215632915496826171875"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.12167599899735"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-3.061892120459039912105936309671960771083831787109375"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.00855146025699900051708635118075108039192855358123779296875"),
      "secondSanctionGapWeight" to BigDecimal("-0.6912829426589344183895491369185037910938262939453125"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasScoreSquared" to BigDecimal("0"),
      "totalWeight" to BigDecimal("-1.843413998935392193529720257927362325744979898445308208465576171875"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 13.66,
      band = RiskBand.LOW,
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
      "ageGenderPolynomialWeight" to BigDecimal("-2.7188340783678792836328195862971579543909683707170188426971435546875"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.0250113803601321"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-2.603445812880039955672373253037221729755401611328125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.00605257512936100035283448761447289143688976764678955078125"),
      "secondSanctionGapWeight" to BigDecimal("-0.51156004593935844315666372494888491928577423095703125"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasScoreSquared" to BigDecimal("0"),
      "suitableAccommodationWeight" to BigDecimal("0.08480491583557529799985985619059647433459758758544921875"),
      "unemployedWeight" to BigDecimal("0.0635567467618753934033293262473307549953460693359375"),
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
      "totalWeight" to BigDecimal("-1.1349442032394994942407262726543404340873166802339255809783935546875"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 24.32,
      band = RiskBand.LOW,
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
      BigDecimal("-2.63929568530589167565907415878394270958295919626834802329540252685546875"),
      context.allReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.57873331384829756161847225115479886881075799465179443359375"),
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
      "ageGenderPolynomialWeight" to BigDecimal("-2.63929568530589167565907415878394270958295919626834802329540252685546875"),
      "genderWeight" to BigDecimal.ZERO,
      "offenceGroupWeight" to BigDecimal("0.0250113803601321"),
      "firstSanctionWeight" to BigDecimal.ZERO,
      "secondSanctionWeight" to BigDecimal("-2.603445812880039955672373253037221729755401611328125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.00605257512936100035283448761447289143688976764678955078125"),
      "secondSanctionGapWeight" to BigDecimal("-0.51156004593935844315666372494888491928577423095703125"),
      "offenceFreeMonthsWeight" to BigDecimal("-0.57873331384829756161847225115479886881075799465179443359375"),
      "copasScore" to BigDecimal.ZERO,
      "copasScoreSquared" to BigDecimal.ZERO,
      "suitableAccommodationWeight" to BigDecimal.ZERO,
      "unemployedWeight" to BigDecimal("0.0635567467618753934033293262473307549953460693359375"),
      "liveInRelationshipWeight" to BigDecimal.ZERO,
      "relationshipQualityWeight" to BigDecimal.ZERO,
      "multiplicativeRelationshipWeight" to BigDecimal.ZERO,
      "domesticViolenceWeight" to BigDecimal.ZERO,
      "regularOffendingActivitiesWeight" to BigDecimal.ZERO,
      "drugMotivationWeight" to BigDecimal.ZERO,
      "chronicDrinkingProblemsWeight" to BigDecimal.ZERO,
      "bingeDrinkingProblemsWeight" to BigDecimal.ZERO,
      "impulsivityProblemsWeight" to BigDecimal.ZERO,
      "proCriminalAttitudesWeight" to BigDecimal.ZERO,
      "heroinUsageWeight" to BigDecimal.ZERO,
      "otherOpiateUsageWeight" to BigDecimal.ZERO,
      "crackCocaineUsageWeight" to BigDecimal.ZERO,
      "powderCocaineUsageWeight" to BigDecimal.ZERO,
      "misusedPrescriptionDrugUsageWeight" to BigDecimal.ZERO,
      "benzodiazepinesUsageWeight" to BigDecimal.ZERO,
      "cannabisUsageWeight" to BigDecimal.ZERO,
      "steroidUsageWeight" to BigDecimal.ZERO,
      "otherDrugUsageWeight" to BigDecimal.ZERO,
      "totalWeight" to BigDecimal("-2.41397781906080095598922525832643326548776485651615075767040252685546875"),
    )

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(expectedFeatureValues, context.allReoffendingPredictor?.featureValues)
  }
}
