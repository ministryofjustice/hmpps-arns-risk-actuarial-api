package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.pni

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.RiskBand

data class PNIRequestValidated(
    val supervisionStatus: SupervisionStatus,
    val hasEverCommittedSexualOffence: Boolean?,
    val isARiskOfSexualHarm: Boolean?,
    val sexualPreoccupation: ProblemLevel?,
    val offenceRelatedSexualInterests: ProblemLevel?,
    val emotionalCongruenceWithChildren: ProblemLevel?,
    val problemSolvingSkills: ProblemLevel?,
    val difficultiesCoping: ProblemLevel?,
    val proCriminalAttitudes: ProblemLevel?,
    val hostileOrientation: ProblemLevel?,
    val currentRelationshipWithFamilyMembers: ProblemLevel?,
    val previousCloseRelationships: ProblemLevel?,
    val easilyInfluencedByCriminalAssociates: ProblemLevel?,
    val controllingOrAggressiveBehaviour: ProblemLevel?,
    val impulsivityProblems: ProblemLevel?,
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
