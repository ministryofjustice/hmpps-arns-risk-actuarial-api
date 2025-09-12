package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.snsv

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.SupervisionStatus
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.Gender
import java.time.LocalDate

data class SNSVStaticRequestValidated(
    val gender: Gender,
    val dateOfBirth: LocalDate,
    val assessmentDate: LocalDate,
    val dateOfCurrentConviction: LocalDate,
    val currentOffenceCode: String,
    val totalNumberOfSanctionsForAllOffences: Int,
    val ageAtFirstSanction: Int,
    val supervisionStatus: SupervisionStatus,
    val dateAtStartOfFollowup: LocalDate,
    val totalNumberOfViolentSanctions: Int,
)
