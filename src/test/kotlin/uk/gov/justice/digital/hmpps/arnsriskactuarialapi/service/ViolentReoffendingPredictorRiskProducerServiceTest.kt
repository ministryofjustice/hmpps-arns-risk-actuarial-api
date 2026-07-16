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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
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

  @Mock
  private lateinit var offenceCodeCacheService: OffenceCodeCacheService

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
        "impulsivityProblems",
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
        "impulsivityProblems",
        "temperControl",
      ),
    )

    // Mock out validation
    whenever(validator.validateStatic(request)).thenReturn(emptyList())
    whenever(validator.validateDynamic(request)).thenReturn(listOf(expectedDynamicValidationError))
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.ACQUISITIVE_VIOLENCE)

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("3.123244332355790131572348400368355214595794677734375"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.064735129815653604791494371735460777728121684049256145954132080078125"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("-0.0032926198476618"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.1099508455483400037877572685829363763332366943359375"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.01353161354898619987696672950505671906284987926483154296875"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("0.037058865006339002101665158761534257791936397552490234375"),
      "secondSanctionGapWeight" to BigDecimal("-0.828714856603948835100936776143498718738555908203125"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.63758298544199690944625227297797298575687818811275064945220947265625"),
      "totalWeight" to BigDecimal("-0.497504853444458219329393859815036105231911278679035604000091552734375"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 37.81,
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
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.ACQUISITIVE_VIOLENCE)

    val context = service.getRiskScore(request, emptyContext())

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("1.816874483627910041860786805045790970325469970703125"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.04780274806733149882409517061887527233920991420745849609375"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0.0442727884290873"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.0828982243873899182773357097175903618335723876953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.0136676571864243999454568978535462520085275173187255859375"),
      "neverViolentWeight" to BigDecimal("0"),
      "onceViolentWeight" to BigDecimal("0"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("0.028756191519955398572250260258442722260951995849609375"),
      "secondSanctionGapWeight" to BigDecimal("-0.71836505462946875777419108999310992658138275146484375"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.3076205444777169057478052500811560587834492253023199737071990966796875"),
      "suitableAccommodationWeight" to BigDecimal("0.10543024551280499867544904191163368523120880126953125"),
      "unemployedWeight" to BigDecimal("0.06636313286463220439959087570969131775200366973876953125"),
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
      "totalWeight" to BigDecimal("-0.2108205261414196645295062403902608405559249149519018828868865966796875"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 44.75,
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
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.ACQUISITIVE_VIOLENCE)

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(
      BigDecimal("-0.048731687421886898774203711326435950468294322490692138671875"),
      context.violentReoffendingPredictor?.featureValues?.get(FeatureValue.AGE_GENDER_POLYNOMIAL_WEIGHT.outputName),
    )
    assertEquals(
      BigDecimal("-0.04776554810499039735623150182419749398832209408283233642578125"),
      context.violentReoffendingPredictor?.featureValues?.get(FeatureValue.OFFENCE_FREE_MONTHS_WEIGHT.outputName),
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
      totalNumberOfViolentSanctions = 1,
      dateAtStartOfFollowup = LocalDate.of(2024, 1, 1),
      suitabilityOfAccommodation = ProblemLevel.NO_PROBLEMS,
      isUnemployed = true,
      currentRelationshipWithPartner = ProblemLevel.NO_PROBLEMS,
      evidenceOfDomesticAbuse = false,
      currentRelationshipStatus = CurrentRelationshipStatus.NOT_IN_RELATIONSHIP,
      regularOffendingActivities = ProblemLevel.NO_PROBLEMS,
      motivationToTackleDrugMisuse = null,
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
      hasMethadoneUsage = null,
      currentAlcoholUseProblems = ProblemLevel.NO_PROBLEMS,
      excessiveAlcoholUse = ProblemLevel.NO_PROBLEMS,
      impulsivityProblems = ProblemLevel.NO_PROBLEMS,
      temperControl = ProblemLevel.NO_PROBLEMS,
    )

    // Return no errors from both static and dynamic validation
    whenever(validator.validateStatic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(validator.validateDynamic(requestMissingDateAtStartOfFollowup)).thenReturn(emptyList())
    whenever(offenceCodeCacheService.getActuarialCategory("00001")).thenReturn(ActuarialCategory.ACQUISITIVE_VIOLENCE)

    val expectedFeatureValues = mapOf(
      "twoYearInterceptWeight" to BigDecimal("1.816874483627910041860786805045790970325469970703125"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.048731687421886898774203711326435950468294322490692138671875"),
      "genderWeight" to BigDecimal.ZERO,
      "offenceGroupWeight" to BigDecimal("0.0442727884290873"),
      "firstSanctionWeight" to BigDecimal.ZERO,
      "secondSanctionWeight" to BigDecimal("-1.0828982243873899182773357097175903618335723876953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.0136676571864243999454568978535462520085275173187255859375"),
      "neverViolentWeight" to BigDecimal.ZERO,
      "onceViolentWeight" to BigDecimal("0.1583577951423169871691953858316992409527301788330078125"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("0.0143780957599776992861251301292213611304759979248046875"),
      "secondSanctionGapWeight" to BigDecimal("-0.71836505462946875777419108999310992658138275146484375"),
      "offenceFreeMonthsWeight" to BigDecimal("-0.04776554810499039735623150182419749398832209408283233642578125"),
      "copasScore" to BigDecimal.ZERO,
      "copasViolentOffencesScore" to BigDecimal("-1.3076205444777169057478052500811560587834492253023199737071990966796875"),
      "suitableAccommodationWeight" to BigDecimal.ZERO,
      "unemployedWeight" to BigDecimal("0.06636313286463220439959087570969131775200366973876953125"),
      "liveInRelationshipWeight" to BigDecimal.ZERO,
      "relationshipQualityWeight" to BigDecimal.ZERO,
      "multiplicativeRelationshipWeight" to BigDecimal.ZERO,
      "domesticViolenceWeight" to BigDecimal.ZERO,
      "regularOffendingActivitiesWeight" to BigDecimal.ZERO,
      "drugMotivationWeight" to BigDecimal.ZERO,
      "chronicDrinkingProblemsWeight" to BigDecimal.ZERO,
      "bingeDrinkingProblemsWeight" to BigDecimal.ZERO,
      "impulsivityProblemsWeight" to BigDecimal.ZERO,
      "temperControlWeight" to BigDecimal.ZERO,
      "methadoneUsageWeight" to BigDecimal.ZERO,
      "otherOpiateUsageWeight" to BigDecimal.ZERO,
      "crackCocaineUsageWeight" to BigDecimal.ZERO,
      "powderCocaineUsageWeight" to BigDecimal.ZERO,
      "misusedPrescriptionDrugUsageWeight" to BigDecimal.ZERO,
      "benzodiazepinesUsageWeight" to BigDecimal.ZERO,
      "cannabisUsageWeight" to BigDecimal.ZERO,
      "steroidUsageWeight" to BigDecimal.ZERO,
      "otherDrugUsageWeight" to BigDecimal.ZERO,
      "totalWeight" to BigDecimal("-1.1188024203839530451595259640796331535028684811550192534923553466796875"),
    )

    val context = service.getRiskScore(requestMissingDateAtStartOfFollowup, emptyContext())

    assertEquals(expectedFeatureValues, context.violentReoffendingPredictor?.featureValues)
  }
}
