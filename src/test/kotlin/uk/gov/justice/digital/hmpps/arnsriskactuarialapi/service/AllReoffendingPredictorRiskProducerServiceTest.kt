package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.allreoffendingpredictor.AllReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validAllReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal

class AllReoffendingPredictorRiskProducerServiceTest {

  private val service = AllReoffendingPredictorRiskProducerService()

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
        "dateAtStartOfFollowupCalculated",
      ),
    )
    val expectedDynamicValidationErrors = ValidationError(
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
    val expected = AllReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationErrors, expectedDynamicValidationErrors),
      featureValues = null,
    )

    assertEquals(expected, context.allReoffendingPredictor)
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val context = service.getRiskScore(validAllReoffendingPredictorStaticRiskScoreRequest(), emptyContext())

    val expectedDynamicValidationErrors = ValidationError(
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
    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("5.01702292499072033393758829333819448947906494140625"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.08945517776523581101263157648595392146262383903376758098602294921875000000"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-3.061892120459039912105936309671960771083831787109375"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.008551460256999000517086351180751080391928553581237792968750"),
      "secondSanctionGapWeight" to BigDecimal("-0.69128294265893441838954913691850379109382629394531250000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasScoreSquared" to BigDecimal("0"),
      "totalWeight" to BigDecimal("1.16584122385051119191238491908102492544685446773655712604522705078125000000"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 76.24,
      band = RiskBand.HIGH,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationErrors),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.allReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val context = service.getRiskScore(validAllReoffendingPredictorDynamicRiskScoreRequest(), emptyContext())

    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("3.836541486920140187066863290965557098388671875"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.075523168843552202323133877397143276510860232519917190074920654296875000000"),
      "genderWeight" to BigDecimal("0"),
      "offenceGroupWeight" to BigDecimal("0"),
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
      "totalWeight" to BigDecimal("1.451576952543757790367294773122008866295118423295207321643829345703125000000"),
    )

    val expected = AllReoffendingPredictorObject(
      score = 81.02,
      band = RiskBand.HIGH,
      staticOrDynamic = StaticOrDynamic.DYNAMIC,
      validationErrors = emptyList(),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.allReoffendingPredictor)
  }
}
