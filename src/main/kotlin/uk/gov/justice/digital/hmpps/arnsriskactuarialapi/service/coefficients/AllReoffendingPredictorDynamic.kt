package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.service.coefficients

import uk.gov.justice.digital.hmpps.arnsriskactuarialapi.dto.offencecode.ActuarialCategory
import java.math.BigDecimal

enum class AllReoffendingPredictorDynamic(val label: String, val coefficient: BigDecimal) {

  TWO_YEAR_CONSTANT("Intercept_2", BigDecimal(3.83654148692014)),
  AAI_MALE("maleaai", BigDecimal(-0.110202179628585)),
  AAI_QUADRATIC_MALE("maleaaiaai", BigDecimal(0.0006726723443858)),
  AAI_CUBIC_MALE("maleaaiaaiaai", BigDecimal(0.0000138719445957)),
  AAI_QUARTIC_MALE("maleaaiaaiaaiaai", BigDecimal(-0.0000001610775422)),
  AAI_FEMALE("aaifemale", BigDecimal(-0.0097677485919972)),
  AAI_QUADRATIC_FEMALE("aaiaaifemale", BigDecimal(0.000126702732348)),
  AAI_CUBIC_FEMALE("aaiaaiaaifemale", BigDecimal(-0.0000006094926795)),
  AAI_QUARTIC_FEMALE("aaiaaiaaiaaifemale", BigDecimal(-0.0000000713415862)),
  FEMALE("female", BigDecimal(-2.68801056322021)),
  FIRST_SANCTION("firstsanction", BigDecimal(-3.39824378336932)),
  SECOND_SANCTION("secondsanction", BigDecimal(-2.60344581288004)),
  SANCTION_OCCASIONS("ogrs3_sanctionoccasions", BigDecimal(-0.0030262875646805)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_MALE("malesecondsanctionyearssincefirs", BigDecimal(-0.0319725028712099)),
  YRS_BETWEEN_FIRST_AND_SECOND_SANCTION_FEMALE("femalesecondsanctionyearssincefi", BigDecimal(-0.0311412361734414)),
  OFFENCE_FREE_MONTHS("ofm", BigDecimal(-0.0531180383905312)),
  OFFENCE_FREE_MONTHS_QUADRATIC("ofmofm", BigDecimal(0.0004075218530422)),
  THREE_PLUS_SANCTIONS_COPAS_G_MALE("malethreeplussanctionsogrs4g_rat", BigDecimal(1.24540523044696)),
  THREE_PLUS_SANCTIONS_COPAS_SQUARED_MALE("malethreeplussanctionsogrs4g_rao", BigDecimal(0.0801894262854395)),
  THREE_PLUS_SANCTIONS_COPAS_G_FEMALE("femalethreeplussanctionsogrs4g_r", BigDecimal(0.829951359317464)),
  THREE_PLUS_SANCTIONS_COPAS_SQUARED_FEMALE("femalethreeplussanctionsogrs4g_o", BigDecimal(-0.0430018109602918)),

  ACCOMMODATION_SUITABILITY("S3Q4", BigDecimal(0.0848049158355753)),
  UNEMPLOYED("S4Q2", BigDecimal(0.0317783733809377)),
  LIVE_IN_RELATIONSHIP("S3Q2_PARTNER", BigDecimal(-0.266331545877519)),
  RELATIONSHIP_QUALITY("S6Q4", BigDecimal(0.0364051885005138)),
  QUALITY_OF_LIVE_IN_RELATIONSHIP("S3Q2_PARTNERS6Q4", BigDecimal(0.141655261924428)),
  DOMESTIC_ABUSE("S6Q7_PERP", BigDecimal(0.0635669196833949)),
  ACTIVITIES_ENCOURAGE_OFFENDING("S7Q2", BigDecimal(0.126491602592029)),
  MOTIVATION_TO_TACKLE_DRUG_MISUSE("S8Q8", BigDecimal(0.08615424993393)),
  CHRONIC_DRINKING("S9Q1", BigDecimal(0.0745006703601772)),
  BINGE_DRINKING("S9Q2", BigDecimal(0.0338427436465108)),
  IMPULSIVITY("S11Q2", BigDecimal(0.0421988370095297)),
  PRO_CRIMINAL_ATTITUDE("S12Q1", BigDecimal(0.0439083095441417)),
  HEROIN("heroin", BigDecimal(0.182096496867273)),
  OTHER_OPIATE("otheropiate", BigDecimal(0.17892958261215)),
  CRACK_COCAINE("crack", BigDecimal(0.109095964190426)),
  POWDER_COCAINE("cokepowder", BigDecimal(0.0605135470691152)),
  PRESCRIPTION_DRUG_MISUSE("prescribed", BigDecimal(0.0252240614607483)),
  BENZODIAZEPINES("benzo", BigDecimal(0.0764353348164513)),
  CANNABIS("cannabis", BigDecimal(0.049300440360878)),
  STEROIDS("steroid", BigDecimal(0.202231737251706)),
  OTHER_DRUGS("otherdrug_code_iln", BigDecimal(0.0267794308651123)),
}

fun getAllReoffendingPredictorDynamicOffenceCodeCoefficient(category: ActuarialCategory, currentOffenceCode: String) = when (category) {
  ActuarialCategory.UNKNOWN -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is UNKNOWN, ensure this is validated before the calculation")
  ActuarialCategory.BURGLARY_DOMESTIC -> BigDecimal("0.153587984043406")
  ActuarialCategory.BURGLARY_OTHER -> BigDecimal("0.222442735978286")
  ActuarialCategory.DRUNKENNESS -> BigDecimal("0.523303164370094")
  ActuarialCategory.DRINK_DRIVING -> BigDecimal("-0.196918572042409")
  ActuarialCategory.MOTORING_OFFENCES -> BigDecimal("0.0730361702901059")
  ActuarialCategory.VEHICLE_RELATED_THEFT -> BigDecimal("0.164192212478264")
  ActuarialCategory.FRAUD_AND_FORGERY -> BigDecimal("-0.322533218006326")
  ActuarialCategory.WELFARE_FRAUD -> BigDecimal("-0.841992532871743")
  ActuarialCategory.DRUG_IMPORT_EXPORT_OR_PRODUCTION -> BigDecimal("-0.437670727296335")
  ActuarialCategory.DRUG_POSSESSION_OR_SUPPLY -> BigDecimal("0.0049716912817919")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_ABH_PLUS -> BigDecimal("-0.171350876457525")
  ActuarialCategory.VIOLENCE_AGAINST_THE_PERSON_SUB_ABH -> BigDecimal("-0.171350876457525")
  ActuarialCategory.PUBLIC_ORDER_AND_HARRASSMENT -> BigDecimal("0.0629726265569029")
  ActuarialCategory.WEAPONS_NON_FIREARM -> BigDecimal("-0.171350876457525")
  ActuarialCategory.FIREARMS_MOST_SERIOUS -> BigDecimal("-0.171350876457525")
  ActuarialCategory.FIREARMS_OTHER -> BigDecimal("-0.171350876457525")
  ActuarialCategory.HANDLING_STOLEN_GOODS -> BigDecimal("0.155377725974202")
  ActuarialCategory.CRIMINAL_DAMAGE -> BigDecimal("0.0250113803601321")
  ActuarialCategory.ACQUISITIVE_VIOLENCE -> BigDecimal("-0.177705019766066")
  ActuarialCategory.OTHER_OFFENCES -> BigDecimal("0.0971157179525287")
  ActuarialCategory.ABSCONDING_OR_BAIL -> BigDecimal("0.236777685679456")
  ActuarialCategory.SEXUAL_AGAINST_CHILD -> BigDecimal("0.0175628084853824")
  ActuarialCategory.SEXUAL_NOT_AGAINST_CHILD -> BigDecimal("0.0919115743401314")
  ActuarialCategory.THEFT_NON_MOTOR -> BigDecimal("0.428689161915978")
  ActuarialCategory.NEED_DETAILS_OF_EXACT_OFFENCE -> throw IllegalArgumentException("Offence code mapping for $currentOffenceCode is NEED_DETAILS_OF_EXACT_OFFENCE, ensure this is validated before the calculation")
}
