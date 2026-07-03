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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.ViolentReoffendingPredictorValidator
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ViolentReoffendingPredictorRiskProducerServiceTest {

  @Mock
  private lateinit var validator: ViolentReoffendingPredictorValidator

  @InjectMocks
  private lateinit var service: ViolentReoffendingPredictorRiskProducerService

  @Test
  fun `should return early with both static and dynamic errors when static validation fails`() {
    val request = RiskScoreRequest()

    val expectedStaticValidationError = ValidationError(
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
        "hasMethadoneUsage",
        "currentAlcoholUseProblems",
        "excessiveAlcoholUse",
        "temperControl",
      ),
    )

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(listOf(expectedStaticValidationError))
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationError))

    val context = service.getRiskScore(request, emptyContext())
    val expected = ViolentReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationError, expectedDynamicValidationError),
      featureValues = null,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val request = validViolentReoffendingPredictorStaticRiskScoreRequest()

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
        "hasMethadoneUsage",
        "currentAlcoholUseProblems",
        "excessiveAlcoholUse",
        "temperControl",
      ),
    )

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationError))

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("3.123244332355790131572348400368355214595794677734375"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.064735129815653604791494371735460777728121684049256145954132080078125000000"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.1099508455483400037877572685829363763332366943359375"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.013531613548986199876966729505056719062849879264831542968750"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("0.0185294325031695010508325793807671288959681987762451171875"),
      "secondSanctionGapWeight" to BigDecimal("-0.8287148566039488351009367761434987187385559082031250000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.63758298544199690944625227297797298575687818811275064945220947265625"),
      "totalWeight" to BigDecimal("-0.512741666099965920380226439195803234127879477455280721187591552734375000000"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 37.46,
      band = RiskBand.MEDIUM,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationError),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val request = validViolentReoffendingPredictorDynamicRiskScoreRequest()

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(emptyList())

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("1.816874483627910041860786805045790970325469970703125"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.0478027480673314988240951706188752723392099142074584960937500"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.0828982243873899182773357097175903618335723876953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.01366765718642439994545689785354625200852751731872558593750"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("0.0143780957599776992861251301292213611304759979248046875"),
      "secondSanctionGapWeight" to BigDecimal("-0.718365054629468757774191089993109926581382751464843750000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.3076205444777169057478052500811560587834492253023199737071990966796875"),
      "suitableAccommodationWeight" to BigDecimal("0.10543024551280499867544904191163368523120880126953125"),
      "unemployedWeight" to BigDecimal("0.033181566432316102199795437854845658876001834869384765625"),
      "liveInRelationshipWeight" to BigDecimal("0"),
      "relationshipQualityWeight" to BigDecimal("0.0289758277705754012376626604918783414177596569061279296875"),
      "multiplicativeRelationshipWeight" to BigDecimal("0"),
      "domesticViolenceWeight" to BigDecimal("0"),
      "regularOffendingActivitiesWeight" to BigDecimal(".06554913045842609975455417270495672710239887237548828125"),
      "drugMotivationWeight" to BigDecimal("0.056650134265177898296172998016118071973323822021484375"),
      "chronicDrinkingProblemsWeight" to BigDecimal("0.1225331792155980037417606354210874997079372406005859375"),
      "bingeDrinkingProblemsWeight" to BigDecimal("0.098112617276630398333026050750049762427806854248046875"),
      "impulsivityProblemsWeight" to BigDecimal("0.032358133015797700682281146100649493746459484100341796875"),
      "temperControlWeight" to BigDecimal("0.09190489674470299352737612252894905395805835723876953125"),
      "methadoneUsageWeight" to BigDecimal("0.057690684554484401858065467649794300086796283721923828125"),
      "otherOpiateUsageWeight" to BigDecimal("0"),
      "crackCocaineUsageWeight" to BigDecimal("0"),
      "powderCocaineUsageWeight" to BigDecimal("0"),
      "misusedPrescriptionDrugUsageWeight" to BigDecimal("0"),
      "benzodiazepinesUsageWeight" to BigDecimal("0"),
      "cannabisUsageWeight" to BigDecimal("0.0018647061979710000990950735655360404052771627902984619140625"),
      "steroidUsageWeight" to BigDecimal("0.34219755115315797500130656771943904459476470947265625"),
      "otherDrugUsageWeight" to BigDecimal("0"),
      "totalWeight" to BigDecimal("-0.3026529767628007660154268083743278605624027477460913360118865966796875"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 42.49,
      band = RiskBand.MEDIUM,
      staticOrDynamic = StaticOrDynamic.DYNAMIC,
      validationErrors = emptyList(),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
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
      suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
      isUnemployed = false,
      currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
      evidenceOfDomesticAbuse = false,
      currentRelationshipStatus = CurrentRelationshipStatus.NOT_IN_RELATIONSHIP,
      regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
      motivationToTackleDrugMisuse = MotivationLevel.FULL_MOTIVATION,
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
      hasMethadoneUsage = false,
      currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
      excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
      impulsivityProblems = ProblemLevel.NO_PROBLEMS,
      temperControl = ProblemLevel.NO_PROBLEMS,
    )

    // Mock out validation
    whenever(validator.validateStatic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(validator.validateDynamic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(
      BigDecimal("-0.0487316874218868987742037113264359504682943224906921386718750"),
      context.violentReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.0477655481049903973562315018241974939883220940828323364257812500"),
      context.violentReoffendingPredictor?.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName),
    )
  }
}
