# hmpps-arns-risk-actuarial-api

[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-arns-risk-actuarial-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-arns-risk-actuarial-api "Link to report")
[![Docker Repository on ghcr](https://img.shields.io/badge/ghcr.io-repository-2496ED.svg?logo=docker)](https://ghcr.io/ministryofjustice/hmpps-arns-risk-actuarial-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://arns-risk-actuarial-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

## Running the application locally in Docker (not recommended)

The application comes with a `dev` spring profile that includes default settings for running locally. This is not
necessary when deploying to kubernetes as these values are included in the helm configuration templates -
e.g. `values-dev.yaml`.

There is also a `docker-compose.yml` that can be used to run the latest version from main in docker and also an
instance of Redis, Manage-Offences-Api (wiremock) & HMPPS Auth (required if your service calls out to other services using a token).

```bash
docker compose -f docker-compose.yml down #Stop
docker compose -f docker-compose.yml up #Start
```

### Running the application in Intellij (recommended)

```bash
docker compose pull && docker compose up --scale hmpps-arns-risk-actuarial-api=0
```

will just start a docker instance of Redis, Manage-Offences-Api (wiremock) & HMPPS Auth. The application should then be started with a `dev` active profile
in Intellij. Alternatively use a command like:
`HMPPS_AUTH_URL=https://localhost:8090/auth SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`

### Calling the API locally
You will need to generate a JWT token to call the API. This can be done by running the following command
```bash
curl -X POST http://localhost:8090/auth/oauth/token \
     -H "Authorization: Basic aG1wcHMtYXJucy1yaXNrLWFjdHVhcmlhbC1hcGktZGV2OnRlc3Qtc2VjcmV0" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d "grant_type=client_credentials"
```
Alternatively use the following settings in Postman (in the Authorization)
* Type: OAuth 2.0
* Grant Type: Client Credentials
* Access Token URL: http://localhost:8090/auth/oauth/token
* Client ID: hmpps-arns-risk-actuarial-api-dev
* Client Secret: test-secret
* Client Authentication: Send as Basic Auth header

Use the `access_token` returned in the header of your request to the API e.g. `Authorization: Bearer ${access_token}`

### Initialising Offence-Code Actuarial Mappings

Before calling the `risk-scores` endpoint, you must first initialise the offence-code actuarial mappings in Redis.
To do this, call the `/admin/update-offence-mapping` endpoint (no authentication required).

```bash
 curl -X POST http://localhost:8080/admin/update-offence-mapping
```

### Running the K6 performance tests locally

Ensure the service is running locally then run the following;
```bash
k6 run -e AUTH_URL=http://localhost:8090/auth/oauth/token -e CLIENT_ID=REPLACE_ME -e CLIENT_SECRET=REPLACE_ME ./k6/performance-test.js
```
*Note: Replace CLIENT_ID and CLIENT_SECRET with their actual values!*

If you wish to run the k6 performance tests against a different env you will need to change the hardcoded service URL in the test file.

### Test coverage
We do impose a minimum test coverage of **85%**.

To generate a report to identify missing coverage, please run: `./gradlew koverHtmlReport`
