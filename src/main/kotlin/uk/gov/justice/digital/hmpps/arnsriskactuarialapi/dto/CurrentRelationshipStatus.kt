package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto

enum class CurrentRelationshipStatus(val score: Int) {
  NOT_IN_RELATIONSHIP(0),
  IN_RELATIONSHIP_LIVING_TOGETHER(1),
  IN_RELATIONSHIP_NOT_LIVING_TOGETHER(2),
}
