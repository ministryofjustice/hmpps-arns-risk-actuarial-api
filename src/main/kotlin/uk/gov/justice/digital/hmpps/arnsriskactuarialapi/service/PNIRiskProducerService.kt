package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.anyNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.bothNullSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighOgrs3
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighOvp
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isHighSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isMediumSara
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOgrs3Medium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspDcHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspDcMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspIicHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOspIicMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isOvpMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isRsrHigh
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.isRsrMedium
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.overallNeedsGroupingCalculation
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.addMissingFields
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.validation.pniInitialValidation

@Service
class PNIRiskProducerService : RiskScoreProducer {

  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    var errors = pniInitialValidation(request)

    if (errors.isNotEmpty()) {
      return context.apply { PNI = PNIObject(ProgrammeNeedIdentifier.OMISSION, errors) }
    }

    val requestValidated = PNIRequestValidated(
      inCustodyOrCommunity = request.inCustodyOrCommunity!!,
      hasCommittedSexualOffence = request.hasCommittedSexualOffence,
      riskSexualHarm = request.riskSexualHarm,
      sexualPreoccupation = request.sexualPreoccupation,
      sexualInterestsOffenceRelated = request.sexualInterestsOffenceRelated,
      emotionalCongruence = request.emotionalCongruence,
      proCriminalAttitudes = request.proCriminalAttitudes,
      hostileOrientation = request.hostileOrientation,
      currentRelationshipFamilyMembers = request.currentRelationshipFamilyMembers,
      previousCloseRelationships = request.previousCloseRelationships,
      easilyInfluencedByCriminals = request.easilyInfluencedByCriminals,
      controllingBehaviour = request.controllingBehaviour,
      impulsivityBehaviour = request.impulsivityBehaviour,
      temperControl = request.temperControl,
      ogrs3TwoYear = context.OGRS3?.ogrs3TwoYear,
      ovp = context.OVP?.provenViolentTypeReoffendingTwoYear,
      ovpBand = context.OVP?.band,
      ospDCBand = context.RSR?.ospdcBand,
      ospIICBand = context.RSR?.ospiicBand,
      rsr = context.RSR?.rsrScore?.toInt(),
      saraRiskToPartner = request.saraRiskToPartner,
      saraRiskToOthers = request.saraRiskToOthers,
      problemSolvingSkills = request.problemSolvingSkills,
      difficultiesCoping = request.difficultiesCoping,
    )

    val overallNeed = overallNeedsGroupingCalculation(requestValidated)
    val overallNeedScore = overallNeed.first
    errors = addMissingFields(overallNeed.third.toList(), errors)
    if (overallNeedScore == null) {
      return context.apply { PNI = PNIObject(ProgrammeNeedIdentifier.OMISSION, errors) }
    }

    val overallRisk = when {
      isHighRisk(requestValidated) -> RiskBand.HIGH
      isMediumRisk(requestValidated) -> RiskBand.MEDIUM
      else -> RiskBand.LOW
    }

    val projectedRisk = when {
      isHighRisk(
        requestValidated.copy(
          ospIICBand = requestValidated.ospIICBand ?: RiskBand.VERY_HIGH,
          ospDCBand = requestValidated.ospDCBand ?: RiskBand.VERY_HIGH,
          rsr = requestValidated.rsr ?: 100,
        ),
      ) -> RiskBand.HIGH

      isMediumRisk(requestValidated) -> RiskBand.MEDIUM
      else -> RiskBand.LOW
    }

    val interimResult = when {
      isHighIntensity(requestValidated, overallNeedScore, overallRisk) -> ProgrammeNeedIdentifier.HIGH

      isModerateIntensity(requestValidated, overallNeedScore, overallRisk) -> ProgrammeNeedIdentifier.MODERATE
      else -> ProgrammeNeedIdentifier.ALTERNATIVE
    }

    var pniPathway = interimResult
    // possible omission scenarios
    if (hasMissingAnswers(overallNeed)) {
      if (anyNullSara(requestValidated) && interimResult == ProgrammeNeedIdentifier.ALTERNATIVE) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
      if (projectedRisk != overallRisk) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
      if (bothNullSara(requestValidated) &&
        requestValidated.inCustodyOrCommunity == CustodyOrCommunity.CUSTODY
      ) {
        pniPathway = ProgrammeNeedIdentifier.OMISSION
      }
    }

    return context.apply { PNI = PNIObject(pniPathway, errors) }
  }

  private fun hasMissingAnswers(overallNeed: Triple<NeedScore?, NeedScore?, List<String>>): Boolean = overallNeed.third.isNotEmpty()

  /**
   * High intensity programmes are only available in prisons and will therefore only be provided in these cases.
   */
  internal fun isHighIntensity(
    request: PNIRequestValidated,
    need: NeedScore,
    risk: RiskBand,
  ): Boolean {
    if (request.inCustodyOrCommunity != CustodyOrCommunity.CUSTODY) return false
    return isHighOgrsWithHighOVP(request) ||
      isHighOgrsWithHighSara(request) ||
      isHighNeedWithHighRisk(need, risk)
  }

  /**
   * Moderate Intensity programmes are available in prisons and community.
   */
  internal fun isModerateIntensity(
    request: PNIRequestValidated,
    need: NeedScore,
    risk: RiskBand,
  ): Boolean = (isHighOgrsWithHighOVP(request) && request.inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) ||
    (isHighNeedWithHighRisk(need, risk) && request.inCustodyOrCommunity == CustodyOrCommunity.COMMUNITY) ||
    isHighSara(request) ||
    isMediumSara(request) ||
    isMediumNeedWithHighRisk(need, risk) ||
    isHighNeedWithMediumRisk(need, risk) ||
    isMediumNeedWithMediumRisk(need, risk)

  private fun isHighOgrsWithHighOVP(request: PNIRequestValidated) = isHighOgrs3(request) && isHighOvp(request)

  private fun isHighOgrsWithHighSara(request: PNIRequestValidated) = isHighOgrs3(request) && isHighSara(request)

  private fun isHighNeedWithHighRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.HIGH && risk == RiskBand.HIGH

  private fun isMediumNeedWithHighRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.MEDIUM && risk == RiskBand.HIGH

  private fun isHighNeedWithMediumRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.HIGH && risk == RiskBand.MEDIUM

  private fun isMediumNeedWithMediumRisk(
    need: NeedScore,
    risk: RiskBand,
  ) = need == NeedScore.MEDIUM && risk == RiskBand.MEDIUM

  internal fun isHighRisk(
    requestValidated: PNIRequestValidated,
  ): Boolean = isHighOgrs3(requestValidated) ||
    isHighOvp(requestValidated) ||
    isOspDcHigh(requestValidated) ||
    isOspIicHigh(requestValidated) ||
    isRsrHigh(requestValidated) ||
    isHighSara(requestValidated)

  internal fun isMediumRisk(
    requestValidated: PNIRequestValidated,
  ): Boolean = isOgrs3Medium(requestValidated) ||
    isOvpMedium(requestValidated) ||
    isOspDcMedium(requestValidated) ||
    isOspIicMedium(requestValidated) ||
    isMediumSara(requestValidated) ||
    isRsrMedium(requestValidated)
}
