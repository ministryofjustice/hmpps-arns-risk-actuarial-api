package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CustodyOrCommunity
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

data class PNIRequestValidated(
  val inCustodyOrCommunity: CustodyOrCommunity,
  val hasCommittedSexualOffence: Boolean?,
  val riskSexualHarm: Boolean?,
  val sexualPreoccupation: ProblemLevel?,
  val sexualInterestsOffenceRelated: ProblemLevel?,
  val emotionalCongruence: ProblemLevel?,
  val problemSolvingSkills: ProblemLevel?,
  val difficultiesCoping: ProblemLevel?,
  val proCriminalAttitudes: ProblemLevel?,
  val hostileOrientation: ProblemLevel?,
  val currentRelationshipFamilyMembers: ProblemLevel?,
  val previousCloseRelationships: ProblemLevel?,
  val easilyInfluencedByCriminals: ProblemLevel?,
  val controllingBehaviour: ProblemLevel?,
  val impulsivityBehaviour: ProblemLevel?,
  val temperControl: ProblemLevel?,
  val ogrs3TwoYear: Int?,
  val ovp: Int?,
  val rsr: Int?,
  val ovpBand: RiskBand?,
  val ospDCBand: RiskBand?,
  val ospIICBand: RiskBand?,
  val saraRiskToPartner: RiskBand?,
  val saraRiskToOthers: RiskBand?,
)
