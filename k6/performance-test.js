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

const payload = open('./data/request.json');

export default function (data) {
    const url = "http://localhost:8080/risk-scores/v1";
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${data.access_token}`
        }
    };

    let res = http.post(url, payload, params);
    expect.soft(res.status).toBe(200)
    sleep(1);
}