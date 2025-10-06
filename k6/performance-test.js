import {sleep} from 'k6';
import http from 'k6/http';
import encoding from 'k6/encoding';
import {expect} from "https://jslib.k6.io/k6-testing/0.5.0/index.js";

export const options = {
    vus: 10,
    duration: '30s',
    thresholds: {
        http_req_failed: ['rate<0.01'], // http errors should be less than 1%
        http_req_duration: ['p(95)<50'], // 95 percent of response times must be below 50ms
    },
};

export function setup() {
    const url = __ENV.AUTH_URL;
    const clientId = __ENV.CLIENT_ID;
    const clientSecret = __ENV.CLIENT_SECRET;
    return authenticate(url, clientId, clientSecret);
}

/**
 *
 * @param {string} authUrl - The endpoint for the auth server
 * @param {string} clientId - The client ID
 * @param {string} clientSecret - The client secret
 */
export function authenticate(
    authUrl,
    clientId,
    clientSecret
) {
    const encodedCredentials = encoding.b64encode(`${clientId}:${clientSecret}`);
    const params = {
        headers: {
            Authorization: `Basic ${encodedCredentials}`,
        },
    };
    const requestBody = {
        grant_type: "client_credentials"
    };
    const response = http.post(authUrl, requestBody, params)
    return response.json();
}

export default function (data) {
    const url = "http://localhost:8080/risk-scores/v1";
    const payload = `
    {
  "version": "V1_0",
  "gender": "MALE",
  "dateOfBirth": "1980-01-01",
  "assessmentDate": "2025-01-01",
  "dateOfCurrentConviction": "2020-01-01",
  "currentOffenceCode": "02700",
  "totalNumberOfSanctionsForAllOffences": 1,
  "ageAtFirstSanction": 40,
  "supervisionStatus": "COMMUNITY",
  "dateAtStartOfFollowupUserInput": "2040-01-01",
  "dateAtStartOfFollowupCalculated": "2040-01-01",
  "totalNumberOfViolentSanctions": 1,
  "didOffenceInvolveCarryingOrUsingWeapon": false,
  "suitabilityOfAccommodation": "NO_PROBLEMS",
  "isUnemployed": false,
  "currentRelationshipWithPartner": "NO_PROBLEMS",
  "currentAlcoholUseProblems": "NO_PROBLEMS",
  "excessiveAlcoholUse": "NO_PROBLEMS",
  "impulsivityProblems": "NO_PROBLEMS",
  "temperControl": "NO_PROBLEMS",
  "proCriminalAttitudes": "NO_PROBLEMS",
  "evidenceOfDomesticAbuse": true,
  "previousConvictions": [
    "WOUNDING_GBH"
  ],
  "hasEverCommittedSexualOffence": true,
  "isCurrentOffenceSexuallyMotivated": false,
  "totalContactAdultSexualSanctions": 5,
  "totalContactChildSexualSanctions": 5,
  "totalNonContactSexualOffences": 5,
  "totalIndecentImageSanctions": 5,
  "isCurrentlyOfNoFixedAbodeOrTransientAccommodation": true,
  "workRelatedSkills": "SIGNIFICANT_PROBLEMS",
  "problemsWithReadingWritingNumeracy": "SIGNIFICANT_PROBLEMS",
  "hasProblemsWithReading": true,
  "hasProblemsWithNumeracy": true,
  "learningDifficulties": "SIGNIFICANT_PROBLEMS",
  "professionalOrVocationalQualifications": "ANY_QUALIFICATION",
  "hasPeerGroupInfluences": true,
  "influenceFromCriminalAssociates": "SOME_PROBLEMS",
  "recklessnessAndRiskTakingBehaviour": "SOME_PROBLEMS",
  "difficultiesCoping": "SOME_PROBLEMS",
  "attitudesTowardsSelf": "SOME_PROBLEMS",
  "problemSolvingSkills": "SOME_PROBLEMS",
  "awarenessOfConsequences": "YES",
  "understandsOtherPeoplesViews": "NO_PROBLEMS",
  "regularOffendingActivities": "SOME_PROBLEMS",
  "currentDrugMisuse": "SIGNIFICANT_PROBLEMS",
  "motivationToTackleDrugMisuse": "FULL_MOTIVATION",
  "applyOPDOverride": null,
  "didOffenceInvolveViolenceOrThreatOfViolence": true,
  "didOffenceInvolveExcessiveUseOfViolence": false,
  "didOffenceInvolveArson": false,
  "offenceMotivationEmotionalState": false,
  "isAnalysisOfOffenceIssuesLinkedToRisk": true,
  "hasAccommodationIssuesLinkedToRisk": true,
  "experienceOfChildhood": "NO_PROBLEMS",
  "domesticAbuseAgainstPartner": true,
  "domesticAbuseAgainstFamily": true,
  "relationshipIssuesLinkedToRisk": true,
  "currentPsychologicalProblems": "SIGNIFICANT_PROBLEMS",
  "currentPsychiatricProblems": "SOME_PROBLEMS",
  "hasCurrentPsychiatricTreatment": false,
  "areEmotionalIssuesLinkedToRisk": true,
  "areThinkingAndBehaviourIssuesLinkedToRisk": true,
  "hasCustodialSentence": true,
  "overRelianceOnOthersForFinancialSupport": "SIGNIFICANT_PROBLEMS",
  "manipulativeOrPredatoryBehaviour": "SOME_PROBLEMS",
  "isEvidenceOfChildhoodBehaviouralProblems": false,
  "hasHistoryOfPsychiatricTreatment": false,
  "hasBeenOnMedicationForMentalHealthProblems": true,
  "hasEverBeenInSpecialHospitalOrRegionalSecureUnit": false,
  "hasSelfHarmOrAttemptedSuicide": true,
  "attitudeTowardsSupervisionOrLicence": "SOME_PROBLEMS",
  "controllingOrAggressiveBehaviour": "SOME_PROBLEMS",
  "hasEscapedOrAbsconded": false,
  "doesRecogniseImpactOfOffendingOnOthers": true,
  "hasDisplayedObsessiveBehaviourLinkedToOffending": false,
  "hasAssaultedOrThreatenedStaff": false,
  "isEligibleForMappa": false,
  "overallRiskForAssessment": "HIGH",
  "highestRiskLevelOverAllAssessments": "HIGH",
  "hasControlIssues": false,
  "sexualPreoccupation": "SIGNIFICANT_PROBLEMS",
  "offenceRelatedSexualInterests": "SIGNIFICANT_PROBLEMS",
  "emotionalCongruenceWithChildren": "SIGNIFICANT_PROBLEMS",
  "saraRiskToOthers": "HIGH",
  "hostileOrientation": "NO_PROBLEMS",
  "currentRelationshipWithFamilyMembers": "NO_PROBLEMS",
  "previousCloseRelationships": "NO_PROBLEMS",
  "easilyInfluencedByCriminalAssociates": "NO_PROBLEMS"
}
    `;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.access_token}`
        }
    };

    let res = http.post(url, payload, params);

    expect.soft(res.status).toBe(200);
    sleep(1);
}