package uk.gov.justice.digital.hmpps.arnsriskactuarialapi.regression

val rsrInputFields = mapOf(
  InputField(arnsFieldName = "assessmentDate", oasysFieldName = "assessment_date") to
    listOf("2025-01-01"),

  InputField(arnsFieldName = "gender", oasysFieldName = "gender", transformFunction = convertGender()) to
    listOf("MALE", "FEMALE"),

  InputField(arnsFieldName = "dateOfBirth", oasysFieldName = "DOB") to
    listOf("1980-06-12", "1990-01-01", "2000-10-02"),

  InputField(arnsFieldName = "dateOfCurrentConviction", oasysFieldName = "date_of_current_conviction") to
    listOf("2025-01-01", "1999-01-02", "2023-07-09"),

  InputField(arnsFieldName = "currentOffenceCode", oasysFieldName = "offence") to
    listOf(
      // absconding/bail
      "19354",
      // acquisitive violence
      "03401",
      // burglary (domestic)
      "02802",
      // burglary (other)
      "18500",
      // criminal damage
      "05715",
      // drink driving
      "80313",
      // drug import/export/production
      "07750",
      // drug possession/supply
      "09267",
      // drunkenness
      "14112",
      // fraud and forgery
      "03806",
      // motoring offences
      "17302",
      // other offences
      "06805",
      // public order and harassment
      "06402",
      // sexual (against child)
      "01808",
      // sexual (not against child)
      "02502",
      // theft (non-motor)
      "11802",
      // vehicle-related theft
      "13003",
      // violence against the person/firearms (most serious)
      "00515",
      // Violence against the person/firearms (other)
      "08107",
      // violence against the person/violence against the person (ABH+)
      "00302",
      // violence against the person/violence against the person (sub-ABH)
      "10401",
      // violence against the person/weapons (non-firearm)
      "19522",
      // welfare fraud
      "15111",
    ),

  InputField(arnsFieldName = "totalNumberOfSanctionsForAllOffences", oasysFieldName = "previous_sanctions") to
    listOf(1, 3, 5),

  InputField(arnsFieldName = "ageAtFirstSanction", oasysFieldName = "age_at_first_sanction") to
    listOf(23, 30),

  InputField(arnsFieldName = "supervisionStatus", oasysFieldName = "CUSTODY_IND", transformFunction = convertSupervisionStatus()) to
    listOf("CUSTODY", "COMMUNITY", "REMAND"),

  InputField(arnsFieldName = "dateAtStartOfFollowupUserInput", oasysFieldName = "community_date") to
    listOf("2028-05-04", "2020-01-09", "2025-01-03"),

  InputField(arnsFieldName = "totalNumberOfViolentSanctions", oasysFieldName = "Violent_Sanctions_count") to
    listOf(1, 3, 5),

  InputField(arnsFieldName = "hasEverCommittedSexualOffence", oasysFieldName = "Sexual_Element", transformFunction = convertBoolean()) to
    listOf(false, true),

  InputField(arnsFieldName = "totalContactAdultSexualSanctions", oasysFieldName = "Contact_Adult_score") to
    listOf(0, 1, 2, 3, 4, 5),

  InputField(arnsFieldName = "totalContactChildSexualSanctions", oasysFieldName = "Contact_Child_score") to
    listOf(0, 1, 2, 3, 4, 5),

  InputField(arnsFieldName = "totalIndecentImageSanctions", oasysFieldName = "Indecent_Images_score") to
    listOf(0, 1, 2, 3, 4, 5),

  InputField(arnsFieldName = "totalNonContactSexualOffences", oasysFieldName = "Paraphilia_score") to
    listOf(0, 1, 2, 3, 4, 5),

  InputField(arnsFieldName = "dateOfMostRecentSexualOffence", oasysFieldName = "DATE_RECENT_SEXUAL_OFFENCE") to
    listOf("2023-01-01", "2025-01-01"),

  InputField(arnsFieldName = "didOffenceInvolveCarryingOrUsingWeapon", oasysFieldName = "S2Q2A", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "suitabilityOfAccommodation", oasysFieldName = "S3Q4", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "isUnemployed", oasysFieldName = "S4Q2", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "currentRelationshipWithPartner", oasysFieldName = "S6Q4", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "evidenceOfDomesticAbuse/domesticAbuseAgainstPartner", oasysFieldName = "S6Q7_PERP", transformFunction = convertDomesticAbuseBooleanListToInt()) to
    listOf(listOf(true, true), listOf(true, false), listOf(false, false)),

  InputField(arnsFieldName = "currentAlcoholUseProblems", oasysFieldName = "S9Q1", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "excessiveAlcoholUse", oasysFieldName = "S9Q2", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "impulsivityProblems", oasysFieldName = "S11Q2", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "temperControl", oasysFieldName = "S11Q4", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "proCriminalAttitudes", oasysFieldName = "S12Q1", transformFunction = convertProblemLevelToInt()) to
    listOf("NO_PROBLEMS", "SOME_PROBLEMS", "SIGNIFICANT_PROBLEMS"),

  InputField(arnsFieldName = "previousConvictions/HOMICIDE", oasysFieldName = "pasthomicide", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/WOUNDING_GBH", oasysFieldName = "pastwoundinggbh", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/KIDNAPPING", oasysFieldName = "pastkidnap", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/FIREARMS", oasysFieldName = "pastfirearm", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/ROBBERY", oasysFieldName = "pastrobbery", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/AGGRAVATED_BURGLARY", oasysFieldName = "pastaggrburg", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/WEAPON", oasysFieldName = "pastweapon", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/CRIMINAL_DAMAGE", oasysFieldName = "pastcdlife", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "previousConvictions/ARSON", oasysFieldName = "pastarson", transformFunction = convertBooleanToInt()) to
    listOf(true, false),

  InputField(arnsFieldName = "snsvStaticOrDynamic", oasysFieldName = "static_calc", transformFunction = convertStaticDynamicToBoolean()) to
    listOf("STATIC", "DYNAMIC"),

  InputField(arnsFieldName = "isCurrentOffenceSexuallyMotivated", oasysFieldName = "CURR_SEX_OFF_MOTIVATION", transformFunction = convertBooleanWithOmission()) to
    listOf(null, true, false),

  InputField(arnsFieldName = "isCurrentOffenceAgainstVictimStranger", oasysFieldName = "STRANGER_VICTIM", transformFunction = convertBoolean()) to
    listOf(true, false),

  InputField(arnsFieldName = "mostRecentOffenceDate", oasysFieldName = "MOST_RECENT_OFFENCE") to
    listOf("2025-01-01", "2022-09-07", "2003-09-01"),
)
