package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.violentreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.CurrentRelationshipStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.MotivationLevel
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

sealed class ViolentReoffendingPredictorRequestValidated {

  data class Static(
    val assessmentDate: LocalDate,
    val dateOfBirth: LocalDate,
    val dateOfCurrentConviction: LocalDate,
    val ageAtFirstSanction: Int,
    val gender: Gender,
    val currentOffenceCode: String,
    val totalNumberOfSanctionsForAllOffences: Int,
    val dateAtStartOfFollowupCalculated: LocalDate,
  ) : ViolentReoffendingPredictorRequestValidated()

  data class Dynamic(
    val staticData: Static,
    val suitabilityOfAccommodation: ProblemLevel,
    val isUnemployed: Boolean,
    val currentRelationshipWithPartner: ProblemLevel,
    val evidenceOfDomesticAbuse: Boolean,
    val currentRelationshipStatus: CurrentRelationshipStatus,
    val regularOffendingActivities: ProblemLevel,
    val motivationToTackleDrugMisuse: MotivationLevel,
    val hasHeroinUsage: Boolean,
    val hasOtherOpiateUsage: Boolean,
    val hasCrackCocaineUsage: Boolean,
    val hasPowderCocaineUsage: Boolean,
    val hasMisusedPrescriptionDrugUsage: Boolean,
    val hasBenzodiazepinesUsage: Boolean,
    val hasCannabisUsage: Boolean,
    val hasSteroidsUsage: Boolean,
    val hasOtherDrugsUsage: Boolean,
    val hasKetamineUsage: Boolean,
    val hasSpiceUsage: Boolean,
    val hasHallucinogensUsage: Boolean,
    val hasSolventsUsage: Boolean,
    val currentAlcoholUseProblems: ProblemLevel,
    val excessiveAlcoholUse: ProblemLevel,
    val impulsivityProblems: ProblemLevel,
    val proCriminalAttitudes: ProblemLevel,
  ) : ViolentReoffendingPredictorRequestValidated()
}
