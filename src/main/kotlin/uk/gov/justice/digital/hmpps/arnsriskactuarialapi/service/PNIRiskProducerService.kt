package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreContext
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskScoreRequest
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIObject
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.PNIRequestValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni.ProgrammeNeedIdentifier

@Service
class PNIRiskProducerService : RiskScoreProducer {
  override fun getRiskScore(
    request: RiskScoreRequest,
    context: RiskScoreContext,
  ): RiskScoreContext {
    // TODO: validate gender and community

    PNIRequestValidated(
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
      ovpRiskBand = context.OVP?.band,
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

    return context.copy(
      PNI =
      PNIObject(request.version, ProgrammeNeedIdentifier.OMISSION, null),
    )
  }
}
