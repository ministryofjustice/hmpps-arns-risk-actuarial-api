package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.transformation

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICHierarchyBand
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICInputValidated
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.ospiic.OSPIICObject

class OSPIICTransformationHelper {
  companion object {

    // transformations

    val noSexualOffenceSanctions: (OSPIICInputValidated) -> Boolean =
      { input ->
        listOf(
          input.totalContactAdultSexualSanctions,
          input.totalContactChildSexualSanctions,
          input.totalIndecentImageSanctions,
          input.totalNonContactSexualOffences,
        ).sum() == 0
      }

    val twoOrMoreIIOCSanctions: (OSPIICInputValidated) -> Boolean =
      { input -> input.totalIndecentImageSanctions >= 2 }

    val oneIIOCSanction: (OSPIICInputValidated) -> Boolean =
      { input -> input.totalIndecentImageSanctions == 1 }

    val twoOrMoreContactChildSexualSanctions: (OSPIICInputValidated) -> Boolean =
      { input -> input.totalContactChildSexualSanctions >= 2 }

    val oneContactChildSexualSanctions: (OSPIICInputValidated) -> Boolean =
      { input -> (input.totalContactChildSexualSanctions == 1) }

    fun ospiicHierarchyBand(input: OSPIICInputValidated): OSPIICHierarchyBand = OSPIICHierarchyBand.entries.find { band -> band.isMatchFor(input) }!!

    // outputs

    fun toOSPIICOutput(hierarchyBand: OSPIICHierarchyBand): OSPIICObject = OSPIICObject(hierarchyBand.band, hierarchyBand.rsrContribution, emptyList())
  }
}
