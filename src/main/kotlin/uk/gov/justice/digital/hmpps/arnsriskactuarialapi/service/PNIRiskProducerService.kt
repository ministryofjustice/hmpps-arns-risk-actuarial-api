package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
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
        .copy(PNI = PNIObject(request.version, ProgrammeNeedIdentifier.OMISSION, errors))
    }

    val requestValidated = PNIRequestValidated(
      version = request.version,
      gender = request.gender!!,
      community = request.community!!,
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
      ovpRiskBand = context.OVP?.band,
      rsr = null, // TODO
      ospDCCRiskBand = null, // TODO
      ospIICIRiskBand = null, // TODO
      ospRiskBand = null, // TODO
      rsrRiskBand = null, // TODO
      snsvRiskBand = null, // TODO
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
        PNIObject(request.version, ProgrammeNeedIdentifier.OMISSION, errors),
      )
    }

    val pni = when {
      isHighRisk(requestValidated, overallNeedScore) -> ProgrammeNeedIdentifier.HIGH
      isModerateRisk(requestValidated, overallNeedScore) -> ProgrammeNeedIdentifier.MODERATE
      else -> ProgrammeNeedIdentifier.ALTERNATIVE
    }

    return context.copy(
      PNI =
      PNIObject(request.version, pni, errors),
    )
  }

  private fun isHighRisk(
    requestValidated: PNIRequestValidated,
    overallNeed: NeedScore,
  ): Boolean = isHighOgrs3(requestValidated) ||
    isHighOvp(requestValidated) ||
    isOspDcHigh(requestValidated) ||
    isOspIicHigh(requestValidated) ||
    isRsrHigh(requestValidated) ||
    isHighSara(requestValidated) ||
    overallNeed == NeedScore.HIGH

  private fun isModerateRisk(
    requestValidated: PNIRequestValidated,
    overallNeed: NeedScore,
  ): Boolean = isOgrs3Medium(requestValidated) ||
    isOvpMedium(requestValidated) ||
    isOspDcMedium(requestValidated) ||
    isOspIicMedium(requestValidated) ||
    isRsrMedium(requestValidated) ||
    isMediumSara(requestValidated) ||
    overallNeed == NeedScore.MEDIUM

  private fun isHighOgrs3(requestValidated: PNIRequestValidated) = requestValidated.ogrs3TwoYear?.let { it >= 75 } == true

  private fun isHighOvp(requestValidated: PNIRequestValidated) = requestValidated.ovp?.let { it >= 60.00 } == true

  private fun isOspDcHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospDCCRiskBand == RiskBand.HIGH

  private fun isOspIicHigh(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospIICIRiskBand == RiskBand.HIGH

  fun isHighSara(requestValidated: PNIRequestValidated) = requestValidated.saraRiskToOthers == RiskBand.HIGH ||
    requestValidated.saraRiskToPartner == RiskBand.HIGH

  private fun isRsrMedium(requestValidated: PNIRequestValidated): Boolean {
    val rsrMediumRsr = requestValidated.rsr?.let { it in 1..2 } == true
    if (requestValidated.gender == Gender.FEMALE) {
      return rsrMediumRsr
    }
    // osp scores needs to be ignored for females
    return rsrMediumRsr && requestValidated.ospDCCRiskBand == null && requestValidated.ospIICIRiskBand == null
  }

  private fun isRsrHigh(requestValidated: PNIRequestValidated): Boolean {
    val isHighRsr = requestValidated.rsr?.let { it >= 3 } == true

    if (requestValidated.gender == Gender.FEMALE) {
      return isHighRsr
    }
    return isHighRsr && requestValidated.ospDCCRiskBand == null && requestValidated.ospIICIRiskBand == null
  }

  private fun isOgrs3Medium(requestValidated: PNIRequestValidated) = requestValidated.ogrs3TwoYear?.let { it in 50..74 } == true

  private fun isOvpMedium(requestValidated: PNIRequestValidated) = requestValidated.ovp?.let { it in 30..59 } == true

  private fun isOspDcMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospDCCRiskBand == RiskBand.MEDIUM

  private fun isOspIicMedium(requestValidated: PNIRequestValidated): Boolean = requestValidated.gender == Gender.MALE &&
    requestValidated.ospIICIRiskBand == RiskBand.MEDIUM

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
