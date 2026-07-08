package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import java.math.BigDecimal

enum class ViolentReoffendingPredictorDynamic(val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(1.81687448362791)),
  AAI_MALE("maleaai", BigDecimal(-0.0645236564493287)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(0.0004644696772777)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0106819944976555)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(-0.0000220033910434)),
  AAI_CUBIC_FEMALE("aaiaaiaaifemale", BigDecimal(-0.0000003141984277)),
  AAI_QUARTIC_FEMALE("aaiaaiaaiaaifemale", BigDecimal(-0.0000000174402455)),
  FEMALE("female", BigDecimal(-1.47587262099647)),
  FIRST_SANCTION("firstsanction", BigDecimal(-1.84616323808687)),
  SECOND_SANCTION("secondsanction", BigDecimal(-1.08289822438739)),
  SANCTION_OCCASIONS("ogrs3_sanctionoccasions", BigDecimal(-0.0068338285932122)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE("malesecondsanctionyearssincefirs", BigDecimal(-0.0448978159143418)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE("femalesecondsanctionyearssincefi", BigDecimal(-0.036873809313859)),
  OFFENCE_FREE_MONTHS("ofm", BigDecimal(-0.0527396206616632)),
  OFFENCE_FREE_MONTHS_QUADRATIC("ofmofm", BigDecimal(0.0004145060463894)),
  THREE_PLUS_SANCTIONS_COPAS_V_MALE("malethreeplussanctionsogrs4v_rat", BigDecimal(0.599255596112212)),
  THREE_PLUS_SANCTIONS_COPAS_V_FEMALE("femalethreeplussanctionsogrs4v_r", BigDecimal(0.477448279367104)),
  NEVER_VIOLENT_MALE("maleneverviolent", BigDecimal(-1.72067702170194)),
  NEVER_VIOLENT_FEMALE("femaleneverviolent", BigDecimal(-2.24064936845545)),
  ONCE_VIOLENT("onceviolent", BigDecimal(0.158357795142317)),
  VIOLENT_SANCTIONS("ogrs3_ovp_sanct", BigDecimal(0.0143780957599777)),
  VIOLENT_RATE("ogrs4v_rate_violent", BigDecimal(0.417038098120116)),

  ACCOMMODATION_SUITABILITY("S3Q4", BigDecimal(0.105430245512805)),
  UNEMPLOYED("S4Q2", BigDecimal(0.0331815664323161)),
  LIVE_IN_RELATIONSHIP("S3Q2_PARTNER", BigDecimal(-0.257910738309657)),
  RELATIONSHIP_QUALITY("S6Q4", BigDecimal(0.0289758277705754)),
  QUALITY_OF_LIVE_IN_RELATIONSHIP("S3Q2_PARTNERS6Q4", BigDecimal(0.117527515244771)),
  DOMESTIC_ABUSE("S6Q7_PERP", BigDecimal(0.108473730672409)),
  ACTIVITIES_ENCOURAGE_OFFENDING("S7Q2", BigDecimal(0.0655491304584261)),
  MOTIVATION_TO_TACKLE_DRUG_MISUSE("S8Q8", BigDecimal(0.0566501342651779)),
  CHRONIC_DRINKING("S9Q1", BigDecimal(0.122533179215598)),
  BINGE_DRINKING("S9Q2", BigDecimal(0.0981126172766304)),
  IMPULSIVITY("S11Q2", BigDecimal(0.0323581330157977)),
  TEMPER("S11Q4", BigDecimal(0.091904896744703)),
  METHADONE("methadone", BigDecimal(0.0576906845544844)),
  OTHER_OPIATE("otheropiate", BigDecimal(0.0753935224854228)),
  CRACK_COCAINE("crack", BigDecimal(0.0902595215602798)),
  POWDER_COCAINE("cokepowder", BigDecimal(0.087313111600886)),
  PRESCRIPTION_DRUG_MISUSE("prescribed", BigDecimal(0.139018812980245)),
  BENZODIAZEPINES("benzo", BigDecimal(0.0587469123708279)),
  CANNABIS("cannabis", BigDecimal(0.001864706197971)),
  STEROIDS("steroid", BigDecimal(0.342197551153158)),
  OTHER_DRUGS("otherdrug_code_iln", BigDecimal(0.150053208890266)),
}

fun getViolentReoffendingPredictorDynamicOffenceCodeCoefficient(category: ActuarialCategory, currentOffenceCode: String) = when (category) {
  ActuarialCategory.UNKNOWN -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is UNKNOWN, ensure this is validated before the calculation")
  ActuarialCategory.BURGLARY_DOMESTIC -> BigDecimal("0.235304808003462")
  ActuarialCategory.BURGLARY_OTHER -> BigDecimal("0.191809657473981")
  ActuarialCategory.DRUNKENNESS -> BigDecimal("0.832786589840741")
  ActuarialCategory.DRINK_DRIVING -> BigDecimal("-0.0243380584056904")
  ActuarialCategory.MOTORING_OFFENCES -> BigDecimal("-0.0219990134288181")
  ActuarialCategory.VEHICLE_RELATED_THEFT -> BigDecimal("0.200392887919135")
  ActuarialCategory.FRAUD_AND_FORGERY -> BigDecimal("-0.215055195882386")
  ActuarialCategory.WELFARE_FRAUD -> BigDecimal("-0.937073522898054")
  ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION -> BigDecimal("-0.310067077915746")
  ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY -> BigDecimal("-0.0226462288920859")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS -> BigDecimal("0.077028816727511")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH -> BigDecimal("0.077028816727511")
  ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT -> BigDecimal("0.348649087179239")
  ActuarialCategory.WEAPONS_NON_FIREARM -> BigDecimal("0.077028816727511")
  ActuarialCategory.FIREARMS_MOST_SERIOUS -> BigDecimal("0.077028816727511")
  ActuarialCategory.FIREARMS_OTHER -> BigDecimal("0.077028816727511")
  ActuarialCategory.HANDLING_STOLEN_GOODS -> BigDecimal("0.160583400555203")
  ActuarialCategory.CRIMINAL_DAMAGE -> BigDecimal("0.302881895872264")
  ActuarialCategory.ACQUISITIVE_VIOLENCE -> BigDecimal("0.0442727884290873")
  ActuarialCategory.OTHER_OFFENCES -> BigDecimal("0.186300883246964")
  ActuarialCategory.ABSCONDING_OR_BAIL -> BigDecimal("0.332310036782829")
  ActuarialCategory.SEXUAL_AGAINST_CHILD -> BigDecimal("-0.994785930428512")
  ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD -> BigDecimal("-0.249001167005262")
  ActuarialCategory.THEFT_NON_MOTOR -> BigDecimal("0.257081681109148")
  ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation")
}
