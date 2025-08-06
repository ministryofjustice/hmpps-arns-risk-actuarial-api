package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVDynamicRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.SNSVStaticRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv.ScoreType
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.snsvInitialValidation

@Service
class SNSVRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(request: RiskScoreRequest, context: RiskScoreContext): RiskScoreContext {
    val errors = snsvInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply { SNSV = SNSVObject(null, null, errors) }
    }

    if (isSNSVDynamic(request) == ScoreType.STATIC) {
      val validStaticRequest = SNSVStaticRequestValidated(
        request.gender!!,
        request.dateOfBirth!!,
        request.assessmentDate,
        request.dateOfCurrentConviction!!,
        request.currentOffence!!,
        request.totalNumberOfSanctions!!.toInt(),
        request.ageAtFirstSanction!!.toInt(),
        request.inCustodyOrCommunity!!,
        request.dateAtStartOfFollowup!!,
        request.totalNumberOfViolentSanctions!!.toInt(),
      )
      return context.apply {
        SNSV = getSNSVStaticObject(validStaticRequest)
      }
    } else {
      val validDynamicRequest = SNSVDynamicRequestValidated(
        request.gender!!,
        request.dateOfBirth!!,
        request.assessmentDate,
        request.dateOfCurrentConviction!!,
        request.currentOffence!!,
        request.totalNumberOfSanctions!!.toInt(),
        request.ageAtFirstSanction!!.toInt(),
        request.inCustodyOrCommunity!!,
        request.dateAtStartOfFollowup!!,
        request.totalNumberOfViolentSanctions!!.toInt(),
        request.carryingOrUsingWeapon,
        request.suitabilityOfAccommodation,
        request.employmentStatus,
        request.currentRelationshipWithPartner,
        request.alcoholIsCurrentUseAProblem,
        request.alcoholExcessive6Months,
        request.impulsivityBehaviour,
        request.temperControl,
        request.proCriminalAttitudes,
        request.domesticAbuse,
        request.previousConvictions,
      )
      return context.apply {
        SNSV = getSNSVDynamicObject(validDynamicRequest)
      }
    }
  }

  fun isSNSVDynamic(request: RiskScoreRequest): ScoreType = if (request.carryingOrUsingWeapon != null &&
    request.suitabilityOfAccommodation != null &&
    request.employmentStatus != null &&
    request.currentRelationshipWithPartner != null &&
    request.alcoholIsCurrentUseAProblem != null &&
    request.alcoholExcessive6Months != null &&
    request.impulsivityBehaviour != null &&
    request.temperControl != null &&
    request.proCriminalAttitudes != null &&
    request.domesticAbuse != null &&
    request.previousConvictions != null
  ) {
    ScoreType.DYNAMIC
  } else {
    ScoreType.STATIC
  }

  // todo add runCatching and score in later tickets
  private fun getSNSVStaticObject(
    request: SNSVStaticRequestValidated,
  ): SNSVObject = SNSVObject(null, ScoreType.STATIC, null)

  // todo add runCatching and score in later tickets
  private fun getSNSVDynamicObject(
    request: SNSVDynamicRequestValidated,
  ): SNSVObject = SNSVObject(null, ScoreType.DYNAMIC, null)
}
