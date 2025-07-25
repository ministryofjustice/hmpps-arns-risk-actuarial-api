package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

data class PNIRequestValidated(
  val gender: Gender,
  val community: Boolean,
  val hasCommittedSexualOffence: Boolean?,
  val riskSexualHarm: Boolean?,
  val sexualPreoccupation: ProblemLevel?,
  val sexualInterestsOffenceRelated: ProblemLevel?,
  val emotionalCongruence: ProblemLevel?,
  val proCriminalAttitudes: ProblemLevel?,
  val hostileOrientation: ProblemLevel?,
  val relationshipFamilyMembers: ProblemLevel?,
  val previousCloseRelationships: ProblemLevel?,
  val easilyInfluencedByCriminals: ProblemLevel?,
  val controllingBehaviour: ProblemLevel?,
  val impulsivityBehaviour: ProblemLevel?,
  val temperControl: ProblemLevel?,
  val ogrs3TwoYear: Int?,
  val ovpRiskBand: RiskBand?,
  val ospDCCRiskBand: RiskBand?,
  val ospIICIRiskBand: RiskBand?,
  val ospRiskBand: RiskBand?,
  val rsrRiskBand: RiskBand?,
  val snsvRiskBand: RiskBand?,
  val saraRiskToPartner: RiskBand?,
  val saraRiskToOthers: RiskBand?,
)
