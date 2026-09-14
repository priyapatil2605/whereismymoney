# Debugging Log

## Bug 1 — Unauthorized portfolio access

### Symptom
A user could potentially access a portfolio belonging to another user.

### Root Cause
Authorization was based on user-supplied information instead of the authenticated JWT identity.

### Fix
The authenticated user's ID is extracted from the JWT and repository queries are scoped using that ID.

### Verification
User 2 attempted to access User 1's portfolio and received HTTP 403 Forbidden.

---

## Bug 2 — Unauthorized transaction access returned HTTP 500

### Symptom
A user attempting to access another user's transaction returned an incorrect server error.

### Root Cause
Authorization failure was represented using a generic runtime exception.

### Fix
The service now throws a `ResponseStatusException` with HTTP 403 and the global exception handler converts it into a structured response.

### Verification
Unauthorized transaction access returns HTTP 403.

---

## Bug 3 — Missing JWT environment variable

### Symptom
Spring Boot failed to start because `JWT_SECRET` could not be resolved.

### Root Cause
The environment variable was saved at the Windows User level but was not available in the existing PowerShell process.

### Fix
The variable was loaded into the current PowerShell session before starting Spring Boot.

### Verification
Spring Boot starts successfully and JWT authentication works.

---

## Bug 4 — Password hash exposed in API response

### Symptom
The `passwordHash` field appeared in user JSON responses.

### Root Cause
Jackson serialized the entity field normally.

### Fix
`passwordHash` was configured with:

`@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)`

### Verification
Password hashes are no longer returned in API responses.

---

## Bug 5 — Missing Claude API key

### Symptom
Spring Boot failed during startup because `CLAUDE_API_KEY` could not be resolved.

### Root Cause
The environment variable was not available in the current PowerShell session.

### Fix
The environment variable was loaded into the current session before starting Spring Boot.

### Verification
Spring Boot starts successfully with the Claude configuration.

---

## Bug 6 — JUnit test compiled as production code

### Symptom
Maven reported that JUnit and Mockito packages did not exist.

### Root Cause
`AuthServiceImplTest.java` was placed under:

`src/main/java`

instead of:

`src/test/java`

### Fix
The test was moved to the correct test source directory and the stale production copy was removed.

### Verification
Maven successfully compiled and executed the test.

Result:

`Tests run: 6, Failures: 0, Errors: 0`

---

# Phase 6 Testing Summary

- Authentication unit tests: 5
- Application context test: 1
- Transaction service tests: 4
- Support ticket service tests: 5

All tests must pass before Phase 6 is considered complete.