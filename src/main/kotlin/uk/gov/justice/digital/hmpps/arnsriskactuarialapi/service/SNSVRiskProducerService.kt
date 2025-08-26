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
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentAlcoholUseProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.currentRelationshipWithPartnerWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.didOffenceInvolveCarryingOrUsingWeaponWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.domesticViolenceWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.excessiveAlcoholUseWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.get2YearInterceptWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getAgeGenderPolynomialWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getGenderWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getMonthsSinceLastSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getNumberOfSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getThreePlusSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getTotalSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolenceRateWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentHistoryWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getViolentSanctionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.getYearsBetweenFirstAndSecondSanctionWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.impulsivityProblemsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.isUnemployedWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.previousConvictionsWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.proCriminalAttitudesWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.suitabilityOfAccommodationWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SNSVTransformationHelper.Companion.temperControlWeight
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.getNullValuesFromProperties
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.snsvInitialValidation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.roundToNDecimals
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.utils.sigmoid
import kotlin.getOrElse

@Service
class SNSVRiskProducerService : RiskScoreProducer {

  companion object {
    val SNSV_DYNAMIC_ADDITIONAL_REQUIRED_PROPERTIES = listOf(
      RiskScoreRequest::didOffenceInvolveCarryingOrUsingWeapon,
      RiskScoreRequest::suitabilityOfAccommodation,
      RiskScoreRequest::isUnemployed,
      RiskScoreRequest::currentRelationshipWithPartner,
      RiskScoreRequest::currentAlcoholUseProblems,
      RiskScoreRequest::excessiveAlcoholUse,
      RiskScoreRequest::impulsivityProblems,
      RiskScoreRequest::temperControl,
      RiskScoreRequest::proCriminalAttitudes,
      RiskScoreRequest::evidenceOfDomesticAbuse,
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
          fields = emptyList(),
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
    this.supervisionStatus!!,
    this.dateAtStartOfFollowup!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
    this.didOffenceInvolveCarryingOrUsingWeapon!!,
    this.suitabilityOfAccommodation!!,
    this.isUnemployed!!,
    this.currentRelationshipWithPartner!!,
    this.currentAlcoholUseProblems!!,
    this.excessiveAlcoholUse!!,
    this.impulsivityProblems!!,
    this.temperControl!!,
    this.proCriminalAttitudes!!,
    this.evidenceOfDomesticAbuse!!,
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
    this.supervisionStatus!!,
    this.dateAtStartOfFollowup!!,
    this.totalNumberOfViolentSanctions!!.toInt(),
  )

  private fun snvsStaticSum(request: SNSVStaticRequestValidated): Double = listOf(
    get2YearInterceptWeight(false),
    getGenderWeight(request.gender, false),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, false),
    offenceGroupParametersService.getSNSVStaticWeighting(request.currentOffenceCode),
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false),
    getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, false),
    getYearsBetweenFirstAndSecondSanctionWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      false,
    ),
    getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
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
    getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, false),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, false), //
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      false,
    ), //
    offenceGroupParametersService.getSNSVVATPStaticWeighting(request.currentOffenceCode),
  ).sum()

  private fun snvsDynamicSum(request: SNSVDynamicRequestValidated): Double = listOf(
    get2YearInterceptWeight(true),
    getGenderWeight(request.gender, true),
    getAgeGenderPolynomialWeight(request.gender, request.dateOfBirth, request.dateAtStartOfFollowup, true),
    offenceGroupParametersService.getSNSVDynamicWeighting(request.currentOffenceCode),
    getNumberOfSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true),
    getTotalSanctionWeight(request.totalNumberOfSanctionsForAllOffences, true),
    getYearsBetweenFirstAndSecondSanctionWeight(
      request.totalNumberOfSanctionsForAllOffences,
      request.gender,
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      true,
    ),
    getMonthsSinceLastSanctionWeight(
      request.supervisionStatus,
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
    getViolentHistoryWeight(request.totalNumberOfViolentSanctions, request.gender, true),
    getViolentSanctionsWeight(request.totalNumberOfViolentSanctions, true),
    getViolenceRateWeight(
      request.dateOfBirth,
      request.dateOfCurrentConviction,
      request.ageAtFirstSanction,
      request.totalNumberOfViolentSanctions,
      true,
    ),
    offenceGroupParametersService.getSNSVVATPDynamicWeighting(request.currentOffenceCode),
    // Dynamic Additions
    didOffenceInvolveCarryingOrUsingWeaponWeight(request.didOffenceInvolveCarryingOrUsingWeapon),
    suitabilityOfAccommodationWeight(request.suitabilityOfAccommodation),
    isUnemployedWeight(request.isUnemployed),
    currentRelationshipWithPartnerWeight(request.currentRelationshipWithPartner),
    currentAlcoholUseProblemsWeight(request.currentAlcoholUseProblems),
    excessiveAlcoholUseWeight(request.excessiveAlcoholUse),
    impulsivityProblemsWeight(request.impulsivityProblems),
    temperControlWeight(request.temperControl),
    proCriminalAttitudesWeight(request.proCriminalAttitudes),
    domesticViolenceWeight(request.evidenceOfDomesticAbuse),
    previousConvictionsWeight(request.previousConvictions),
  ).sum()
}
