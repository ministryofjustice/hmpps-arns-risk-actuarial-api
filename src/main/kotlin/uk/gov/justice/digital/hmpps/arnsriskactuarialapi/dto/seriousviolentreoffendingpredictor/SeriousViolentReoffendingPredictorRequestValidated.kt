package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.seriousviolentreoffendingpredictor

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.PreviousConviction
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ProblemLevel
import java.time.LocalDate

sealed interface SeriousViolentReoffendingPredictorRequestValidated {

  data class Static(
    val assessmentDate: LocalDate,
    val dateOfBirth: LocalDate,
    val dateOfCurrentConviction: LocalDate,
    val ageAtFirstSanction: Int,
    val gender: Gender,
    val currentOffenceCode: String,
    val totalNumberOfSanctionsForAllOffences: Int,
    val totalNumberOfViolentSanctions: Int,
    val dateAtStartOfFollowup: LocalDate,
  ) : SeriousViolentReoffendingPredictorRequestValidated

  data class Dynamic(
    val staticData: Static,
    val didOffenceInvolveCarryingOrUsingWeapon: Boolean,
    val suitabilityOfAccommodation: ProblemLevel,
    val isUnemployed: Boolean,
    val currentAlcoholUseProblems: ProblemLevel,
    val temperControl: ProblemLevel,
    val proCriminalAttitudes: ProblemLevel,
    val previousConvictions: List<PreviousConviction>,
  ) : SeriousViolentReoffendingPredictorRequestValidated
}
