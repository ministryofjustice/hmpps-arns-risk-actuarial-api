import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp.IICSRPHierarchy
import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.iicsrp.IICSRPInputValidated

object IICSRPTransformationHelper {

  fun iicsrpHierarchy(input: IICSRPInputValidated): IICSRPHierarchy = when {
    input.totalIndecentImageSanctions >= 2 -> IICSRPHierarchy.TwoOrMoreIICSanctions
    input.totalIndecentImageSanctions == 1 -> IICSRPHierarchy.OneIICSanction
    input.totalContactChildSexualSanctions >= 2 -> IICSRPHierarchy.TwoOrMoreContactChildSexualSanctions
    input.totalContactChildSexualSanctions == 1 -> IICSRPHierarchy.OneContactChildSexualSanctions
    hasNoSexualOffenceSanctions(input) -> IICSRPHierarchy.NoSexualOffenceSanctions
    else -> IICSRPHierarchy.AllOthers
  }

  private fun hasNoSexualOffenceSanctions(input: IICSRPInputValidated): Boolean = (
    input.totalContactAdultSexualSanctions +
      input.totalContactChildSexualSanctions +
      input.totalIndecentImageSanctions +
      input.totalNonContactSexualOffences
    ) == 0
}
