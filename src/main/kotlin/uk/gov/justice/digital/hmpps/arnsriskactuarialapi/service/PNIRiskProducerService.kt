package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.NeedScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.SexDomainScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation.getOverallNeedClassification
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
      return context
        .copy(PNI = PNIObject(ProgrammeNeedIdentifier.OMISSION, errors))
    }

    val requestValidated = PNIRequestValidated(
      gender = request.gender!!,
      inCustodyOrCommunity = request.inCustodyOrCommunity,
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
      ospDCBand = null, // TODO
      ospIICBand = null, // TODO
      rsr = null, // TODO
      saraRiskToPartner = request.saraRiskToPartner,
      saraRiskToOthers = request.saraRiskToOthers,
      problemSolvingSkills = request.problemSolvingSkills,
      difficultiesCoping = request.difficultiesCoping,
    )

    val overallNeed = overallNeedsGroupingCalculation(requestValidated)
    val overallNeedScore = overallNeed.first
    if (overallNeedScore == null) {
      errors = addMissingFields(overallNeed.second.toMutableList(), errors)
      return context.copy(
        PNI =
        PNIObject(ProgrammeNeedIdentifier.OMISSION, errors),
      )
    }

    val risk = when {
      isHighRisk(requestValidated) -> RiskBand.HIGH
      isMediumRisk(requestValidated) -> RiskBand.MEDIUM
      else -> RiskBand.LOW
    }

    val pniPathway = when {
      isHighIntensity(requestValidated, overallNeedScore, risk) -> ProgrammeNeedIdentifier.HIGH

      isModerateIntensity(requestValidated, overallNeedScore, risk) -> ProgrammeNeedIdentifier.MODERATE
      else -> ProgrammeNeedIdentifier.ALTERNATIVE
    }

    return context.copy(
      PNI =
      PNIObject(pniPathway, errors),
    )
  }

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
  ): Boolean = requestValidated.inCustodyOrCommunity == CustodyOrCommunity.CUSTODY &&
    isHighOgrs3(requestValidated) ||
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
    isRsrMedium(requestValidated) ||
    isMediumSara(requestValidated)

  private fun isHighOgrs3(requestValidated: PNIRequestValidated) = requestValidated.ogrs3TwoYear?.let { it >= 75 } == true

  private fun isHighOvp(requestValidated: PNIRequestValidated) = requestValidated.ovp?.let { it >= 60.00 } == true

  private fun isOspDcHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospDCBand == RiskBand.HIGH

  private fun isOspIicHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospIICBand == RiskBand.HIGH

  fun isHighSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == RiskBand.HIGH ||
    requestValidated.saraRiskToPartner == RiskBand.HIGH

  private fun isRsrMedium(requestValidated: PNIRequestValidated): Boolean {
    val rsrMediumRsr = requestValidated.rsr?.let { it in 1..2 } == true
    if (requestValidated.gender == Gender.FEMALE) {
      return rsrMediumRsr
    }
    // osp scores needs to be ignored for females
    return rsrMediumRsr && requestValidated.ospDCBand == null && requestValidated.ospIICBand == null
  }

  private fun isRsrHigh(requestValidated: PNIRequestValidated): Boolean {
    val isHighRsr = requestValidated.rsr?.let { it >= 3 } == true

    if (requestValidated.gender == Gender.FEMALE) {
      return isHighRsr
    }
    return isHighRsr && requestValidated.ospDCBand == null && requestValidated.ospIICBand == null
  }

  private fun isOgrs3Medium(requestValidated: PNIRequestValidated) = requestValidated.ogrs3TwoYear?.let { it in 50..74 } == true

  private fun isOvpMedium(requestValidated: PNIRequestValidated) = requestValidated.ovp?.let { it in 30..59 } == true

  private fun isOspDcMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospDCBand == RiskBand.MEDIUM

  private fun isOspIicMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospIICBand == RiskBand.MEDIUM

  fun isMediumSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == RiskBand.MEDIUM ||
    requestValidated.saraRiskToPartner == RiskBand.MEDIUM
}

fun overallNeedsGroupingCalculation(request: PNIRequestValidated): Pair<NeedScore?, List<String>> {
  val (overallSexDomainScore, missingSexDomainScore) = SexDomainScore.overallDomainScore(request)
  val (overallThinkingDomainScore, missingThinkingDomainScore) = ThinkingDomainScore.overallDomainScore(request)
  val (overallRelationshipDomain, missingRelationshipDomain) = RelationshipDomainScore.overallDomainScore(request)
  val (overallSelfManagementDomain, missingSelfManagementDomain) = SelfManagementDomainScore.overallDomainScore(
    request,
  )
  val allMissingFields = listOf(
    missingSexDomainScore,
    missingThinkingDomainScore,
    missingRelationshipDomain,
    missingSelfManagementDomain,
  ).flatten()

  val overallNeedsScore = listOfNotNull(
    overallSexDomainScore,
    overallThinkingDomainScore,
    overallRelationshipDomain,
    overallSelfManagementDomain,
  ).sum()
  if (allMissingFields.isNotEmpty()) {
    return Pair(null, allMissingFields)
  }
  return Pair(getOverallNeedClassification(overallNeedsScore), emptyList())
}
