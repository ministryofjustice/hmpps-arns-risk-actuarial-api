package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.StaticOrDynamic
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationError
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor.ViolentReoffendingPredictorObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.emptyContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorDynamicRiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.validViolentReoffendingPredictorStaticRiskScoreRequest
import java.math.BigDecimal

class ViolentReoffendingPredictorRiskProducerServiceTest {

  private val service = ViolentReoffendingPredictorRiskProducerService()

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
        "hasMethadoneUsage",
        "currentAlcoholUseProblems",
        "excessiveAlcoholUse",
        "temperControl",
      ),
    )
    val expected = ViolentReoffendingPredictorObject(
      score = null,
      band = null,
      staticOrDynamic = null,
      validationErrors = listOf(expectedStaticValidationErrors, expectedDynamicValidationErrors),
      featureValues = null,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
  }

  @Test
  fun `should calculate STATIC predictor when static validation passes but dynamic validation fails`() {
    val context = service.getRiskScore(validViolentReoffendingPredictorStaticRiskScoreRequest(), emptyContext())

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
        "hasMethadoneUsage",
        "currentAlcoholUseProblems",
        "excessiveAlcoholUse",
        "temperControl",
      ),
    )
    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("3.123244332355790131572348400368355214595794677734375"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.064735129815653604791494371735460777728121684049256145954132080078125000000"),
      "genderWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.1099508455483400037877572685829363763332366943359375"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.013531613548986199876966729505056719062849879264831542968750"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("-0.013531613548986199876966729505056719062849879264831542968750"),
      "secondSanctionGapWeight" to BigDecimal("-0.8287148566039488351009367761434987187385559082031250000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.5900709105071192866145221983271706989171434543095529079437255859375"),
      "totalWeight" to BigDecimal("-0.497290637217243998476295673430824795246962821693159639835357666015625000000"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 37.82,
      band = RiskBand.LOW,
      staticOrDynamic = StaticOrDynamic.STATIC,
      validationErrors = listOf(expectedDynamicValidationErrors),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
  }

  @Test
  fun `should calculate DYNAMIC predictor when both static and dynamic validations pass`() {
    val context = service.getRiskScore(validViolentReoffendingPredictorDynamicRiskScoreRequest(), emptyContext())

    val expectedFeatureValues = mapOf<String, BigDecimal>(
      "twoYearInterceptWeight" to BigDecimal("1.816874483627910041860786805045790970325469970703125"),
      "ageGenderPolynomialWeight" to BigDecimal("-0.0478027480673314988240951706188752723392099142074584960937500"),
      "genderWeight" to BigDecimal("0"),
      "firstSanctionWeight" to BigDecimal("0"),
      "secondSanctionWeight" to BigDecimal("-1.0828982243873899182773357097175903618335723876953125"),
      "totalNumberOfSanctionsForAllOffencesWeight" to BigDecimal("-0.01366765718642439994545689785354625200852751731872558593750"),
      "totalNumberOfViolentSanctionsWeight" to BigDecimal("-0.01366765718642439994545689785354625200852751731872558593750"),
      "secondSanctionGapWeight" to BigDecimal("-0.718365054629468757774191089993109926581382751464843750000"),
      "offenceFreeMonthsWeight" to BigDecimal("0"),
      "copasScore" to BigDecimal("0"),
      "copasViolentOffencesScore" to BigDecimal("-1.269681847112195624284182681498045486279124816064722836017608642578125"),
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
      "methadoneUsageWeight" to BigDecimal("0"),
      "heroinUsageWeight" to BigDecimal("0"),
      "otherOpiateUsageWeight" to BigDecimal("0"),
      "crackCocaineUsageWeight" to BigDecimal("0"),
      "powderCocaineUsageWeight" to BigDecimal("0"),
      "misusedPrescriptionDrugUsageWeight" to BigDecimal("0"),
      "benzodiazepinesUsageWeight" to BigDecimal("0"),
      "cannabisUsageWeight" to BigDecimal("0.0018647061979710000990950735655360404052771627902984619140625"),
      "steroidUsageWeight" to BigDecimal("0.34219755115315797500130656771943904459476470947265625"),
      "otherDrugUsageWeight" to BigDecimal("0"),
      "totalWeight" to BigDecimal("-0.350450716898165985641451735423779201283878137473948299884796142578125"),
    )

    val expected = ViolentReoffendingPredictorObject(
      score = 41.33,
      band = RiskBand.LOW,
      staticOrDynamic = StaticOrDynamic.DYNAMIC,
      validationErrors = emptyList(),
      featureValues = expectedFeatureValues,
    )

    assertEquals(expected, context.violentReoffendingPredictor)
  }
}
