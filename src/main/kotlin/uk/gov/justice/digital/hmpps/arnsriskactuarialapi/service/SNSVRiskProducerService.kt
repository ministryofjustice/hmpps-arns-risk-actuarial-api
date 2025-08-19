package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorResponse
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ValidationErrorType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVDynamicRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVStaticRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.excessiveAlcoholUseWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentAlcoholUseProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.carryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentRelationshipWithPartnerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.domesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.isUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getMonthsSinceLastSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getNumberOfSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getThreePlusSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getYearsBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.impulsivityBehaviourWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.previousConvictionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.proCriminalAttitudesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.suitabilityOfAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.getNullValuesFromProperties
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.snsvInitialValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import kotlin.getOrElse

@Service
class SNSVRiskProducerService : RiskScoreProducer {

  companion object {
    val SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES = listOf(
      RiskScoreRequest::carryingOrUsingWeapon,
      RiskScoreRequest::suitabilityOfAccommodation,
      RiskScoreRequest::isUnemployed,
      RiskScoreRequest::currentRelationshipWithPartner,
      RiskScoreRequest::currentAlcoholUseProblems,
      RiskScoreRequest::excessiveAlcoholUse,
      RiskScoreRequest::impulsivityBehaviour,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::proCriminalAttitudes,
      RiskScoreRequest::domesticAbuse,
      RiskScoreRequest::previousConvictions,
    )
  }

  @Autowired
  lateinit var offenceGroupParametersService: OffenceGroupParametersService

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = snsvInitialValidation(request)
    return context.apply {
      SNSV =
        if (errors.isNotEmpty()) {
          SNSVObject(null, null, errors)
        } else {
          val scoreType = isSNSVDynamic(request)
          getSNSVObject(scoreType, request)
        }
    }
  }

  fun getSNSVObject(
    scoreType: ScoreType,
    request: RiskScoreRequest,
  ): SNSVObject = runCatching {
    when (scoreType) {
      ScoreType.STATIC -> {
        snvsStaticSum(request.toSNSVStaticRequestValidated())
      }
      ScoreType.DYNAMIC -> {
        snvsDynamicSum(request.toSNSVDynamicRequestValidated())
      }
    }.let { SNSVObject(it.sigmoid().roundToNDecimals(16), scoreType, emptyList()) }
  }.getOrElse {
    SNSVObject(
      null,
      scoreType,
      arrayListOf(
        ValidationErrorResponse(
          type = ValidationErrorType.UNEXPECTED_VALUE,
          message = "Error: ${it.message}",
          fields = null,
        ),
      ),
    )
  }

  fun isSNSVDynamic(request: RiskScoreRequest): ScoreType = if (getNullValuesFromProperties(request, SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES).isEmpty()) {
    ScoreType.DYNAMIC
  } else {
    ScoreType.STATIC
  }

  private fun RiskScoreRequest.toSNSVDynamicRequestValidated(): SNSVDynamicRequestValidated = SNSVDynamicRequestValidated(
    this.gender!!,
    this.dateOfBirth!!,
    this.assessmentDate,
    this.dateOfCurrentConviction!!,
    this.currentOffenceCode!!,
    this.totalNumberOfSanctionsForAllOffences!!.toInt(),
    this.ageAtFirstSanction!!.toInt(),
    this.inCustodyOrCommunity!!,
    this.dateAtStartOfFollowup!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
    this.carryingOrUsingWeapon!!,
    this.suitabilityOfAccommodation!!,
    this.isUnemployed!!,
    this.currentRelationshipWithPartner!!,
    this.currentAlcoholUseProblems!!,
    this.excessiveAlcoholUse!!,
    this.impulsivityBehaviour!!,
    this.temperControl!!,
    this.proCriminalAttitudes!!,
    this.domesticAbuse!!,
    this.previousConvictions!!,
  )

  private fun RiskScoreRequest.toSNSVStaticRequestValidated() = SNSVStaticRequestValidated(
    this.gender!!,
    this.dateOfBirth!!,
    this.assessmentDate,
    this.dateOfCurrentConviction!!,
    this.currentOffenceCode!!,
    this.totalNumberOfSanctionsForAllOffences!!.toInt(),
    this.ageAtFirstSanction!!.toInt(),
    this.inCustodyOrCommunity!!,
    this.dateAtStartOfFollowup!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
  )

  private fun snvsStaticSum(request: SNSVStaticRequestValidated): Double = listOf(
    get2YearInterceptWeight(false),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.assessmentDate, false),
    offenceGroupParametersService.getSNSVStaticWeighting(request.currentOffenceCode),
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false),
    getYearsBetweenFirstAndSecondSanctionWeight(
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      false,
    ),
    getMonthsSinceLastSanctionWeight(
      request.inCustodyOrCommunity,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      false,
    ),
    getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      false,
    ),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, request.gender, false),
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      false,
    ),
    offenceGroupParametersService.getSNSVVATPStaticWeighting(request.currentOffenceCode),
  ).sum()

  private fun snvsDynamicSum(request: SNSVDynamicRequestValidated): Double = listOf(
    get2YearInterceptWeight(true),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.assessmentDate, true),
    offenceGroupParametersService.getSNSVDynamicWeighting(request.currentOffenceCode),
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true),
    getYearsBetweenFirstAndSecondSanctionWeight(
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      true,
    ),
    getMonthsSinceLastSanctionWeight(
      request.inCustodyOrCommunity,
      request.dateAtStartOfFollowup,
      request.assessmentDate,
      true,
    ),
    getThreePlusSanctionsWeight(
      request.gender,
      request.totalNumberOfSanctionsForAllOffences,
      request.ageAtFirstSanction,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      true,
    ),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, request.gender, true),
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      true,
    ),
    offenceGroupParametersService.getSNSVVATPDynamicWeighting(request.currentOffenceCode),
    // Dynamic Additions
    carryingOrUsingWeaponWeight(request.carryingOrUsingWeapon),
    suitabilityOfAccommodationWeight(request.suitabilityOfAccommodation),
    isUnemployedWeight(request.isUnemployed),
    currentRelationshipWithPartnerWeight(request.currentRelationshipWithPartner),
    currentAlcoholUseProblemsWeight(request.currentAlcoholUseProblems),
    excessiveAlcoholUseWeight(request.excessiveAlcoholUse),
    impulsivityBehaviourWeight(request.impulsivityBehaviour),
    proCriminalAttitudesWeight(request.proCriminalAttitudes),
    domesticViolenceWeight(request.domesticAbuse),
    previousConvictionsWeight(request.previousConvictions),
  ).sum()
}
