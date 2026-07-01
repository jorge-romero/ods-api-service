# core-security — Test Combinations

This document enumerates the test scenarios for the `core-security` module.  
Each scenario is defined by the combination of: **API visibility** × **Auth flow** × **Client type** × **Policy outcome**.

---

## Dimensions

| Dimension | Values |
|---|---|
| API visibility | `PUBLIC`, `SECURED` |
| Auth flow | `CLIENT_CREDENTIALS`, `OBO`, `NONE` (no token) |
| Client type | Internal service, External / third-party app, UI SPA (on behalf of a user) |
| Policy | No rules, `ALLOWED_CLIENTS`, `SCOPE_REQUIRED`, multiple rules |
| Token state | Valid, Expired, Malformed, Missing |

---

## 1. Public API

Public APIs (`ApiDefinition.isPublic = true`) must be accessible regardless of authentication state.

| # | Flow | Token | Client type | Expected |
|---|---|---|---|---|
| P-01 | NONE | Missing | Any | `200 OK` — no token required |
| P-02 | CLIENT_CREDENTIALS | Valid | Internal service | `200 OK` — token accepted but not required |
| P-03 | OBO | Valid | UI SPA | `200 OK` — token accepted but not required |
| P-04 | NONE | Malformed / expired | Any | `200 OK` — public path bypasses JWT validation |

> `PolicyAuthorizationManager` returns `PERMIT` immediately when `apiDef.isPublic() == true`.  
> `AuthTypeEnforcementFilter` skips the flow check when `!apiDef.requiresAuth()`.

---

## 2. Secured API — No Token

| # | API auth types | Token | Expected |
|---|---|---|---|
| S-00-01 | `[CLIENT_CREDENTIALS]` | Missing | `401 Unauthorized` |
| S-00-02 | `[OBO]` | Missing | `401 Unauthorized` |
| S-00-03 | `[CLIENT_CREDENTIALS, OBO]` | Missing | `401 Unauthorized` |
| S-00-04 | `[CLIENT_CREDENTIALS]` | Malformed JWT | `401 Unauthorized` |
| S-00-05 | `[CLIENT_CREDENTIALS]` | Expired JWT | `401 Unauthorized` |

---

## 3. Secured API — CLIENT_CREDENTIALS flow

Token characteristics: has `roles` claim, **no** `scp` claim.  
`AuthFlowResolver` resolves to `CLIENT_CREDENTIALS`.

### 3.1 Flow enforcement (`AuthTypeEnforcementFilter`)

| # | API auth types | Token flow | Expected |
|---|---|---|---|
| CC-F-01 | `[CLIENT_CREDENTIALS]` | CLIENT_CREDENTIALS | passes filter |
| CC-F-02 | `[OBO]` | CLIENT_CREDENTIALS | `403 Forbidden` — wrong flow |
| CC-F-03 | `[CLIENT_CREDENTIALS, OBO]` | CLIENT_CREDENTIALS | passes filter |
| CC-F-04 | `[NONE]` | CLIENT_CREDENTIALS | passes filter (no auth required) |

### 3.2 Policy — ALLOWED_CLIENTS

Config: `{ "clientIds": ["app-a", "app-b"] }`

| # | Client (azp/appid) | Policy | Expected |
|---|---|---|---|
| CC-P-01 | `app-a` (internal service) | ALLOWED_CLIENTS: [app-a, app-b] | `PERMIT` |
| CC-P-02 | `app-c` (external / unknown app) | ALLOWED_CLIENTS: [app-a, app-b] | `DENY` → `403` |
| CC-P-03 | `app-a` | No rules configured | `PERMIT` (empty rules → `ABSTAIN` → `PERMIT`) |
| CC-P-04 | `app-a` | Rules empty list | `DENY` (rules == null or empty → explicit `DENY`) |

### 3.3 Policy — SCOPE_REQUIRED

Config: `{ "scopes": ["api.read"] }`  
CLIENT_CREDENTIALS tokens carry app-level roles, not delegated scopes.

| # | Token scopes (scp) | Policy | Expected |
|---|---|---|---|
| CC-S-01 | none (no scp claim) | SCOPE_REQUIRED: api.read | `DENY` → `403` |
| CC-S-02 | `api.read api.write` | SCOPE_REQUIRED: api.read | `PERMIT` |

### 3.4 Multiple rules (ALLOWED_CLIENTS + SCOPE_REQUIRED)

| # | Client | Scopes | Rules | Expected combined decision |
|---|---|---|---|---|
| CC-M-01 | allowed | required scope present | both PERMIT | `PERMIT` |
| CC-M-02 | allowed | scope missing | PERMIT + DENY | `DENY` → `403` |
| CC-M-03 | not allowed | required scope present | DENY + PERMIT | `DENY` → `403` |
| CC-M-04 | not allowed | scope missing | both DENY | `DENY` → `403` |

---

## 4. Secured API — OBO flow (On-Behalf-Of / delegated)

Token characteristics: has `scp` claim with delegated scopes, identifies an end user via `sub`.  
`AuthFlowResolver` resolves to `OBO`.

### 4.1 Flow enforcement (`AuthTypeEnforcementFilter`)

| # | API auth types | Token flow | Expected |
|---|---|---|---|
| OBO-F-01 | `[OBO]` | OBO | passes filter |
| OBO-F-02 | `[CLIENT_CREDENTIALS]` | OBO | `403 Forbidden` — wrong flow |
| OBO-F-03 | `[CLIENT_CREDENTIALS, OBO]` | OBO | passes filter |

### 4.2 Policy — ALLOWED_CLIENTS

| # | Client (azp) | Policy | Expected |
|---|---|---|---|
| OBO-P-01 | `spa-frontend` (UI SPA) | ALLOWED_CLIENTS: [spa-frontend] | `PERMIT` |
| OBO-P-02 | `mobile-app` (external app) | ALLOWED_CLIENTS: [spa-frontend] | `DENY` → `403` |
| OBO-P-03 | `spa-frontend` | No rules | `PERMIT` |

### 4.3 Policy — SCOPE_REQUIRED

| # | Token scp | Policy | Expected |
|---|---|---|---|
| OBO-S-01 | `api.read api.write` | SCOPE_REQUIRED: api.read | `PERMIT` |
| OBO-S-02 | `api.write` | SCOPE_REQUIRED: api.read | `DENY` → `403` |
| OBO-S-03 | empty / blank scp | SCOPE_REQUIRED: api.read | `DENY` → `403` |

### 4.4 Multiple rules

| # | Client | Scopes | Rules | Expected |
|---|---|---|---|---|
| OBO-M-01 | allowed | required scope present | both PERMIT | `PERMIT` |
| OBO-M-02 | allowed | scope missing | PERMIT + DENY | `DENY` |
| OBO-M-03 | not allowed | required scope present | DENY + PERMIT | `DENY` |

---

## 5. Route not registered (unknown API definition)

`ApiDefinitionResolver` returns empty — `PolicyAuthorizationManager` denies immediately (fail-closed).

| # | Token | Expected |
|---|---|---|
| U-01 | Missing | `403 Forbidden` |
| U-02 | Valid CLIENT_CREDENTIALS | `403 Forbidden` |
| U-03 | Valid OBO | `403 Forbidden` |

---

## 6. JWT claim extraction (`AzureJwtAuthenticationConverter`)

Unit tests for authority and client-id extraction — no HTTP stack needed.

| # | Token claims | Expected authorities | Expected clientId |
|---|---|---|---|
| J-01 | `roles: [ADMIN]`, no scp | `ROLE_ADMIN` | `azp` value |
| J-02 | `scp: "api.read api.write"`, no roles | `SCOPE_api.read`, `SCOPE_api.write` | `azp` value |
| J-03 | both `roles` and `scp` | `ROLE_*` + `SCOPE_*` | `azp` value |
| J-04 | neither claim | empty collection | `azp` value |
| J-05 | `azp` absent, `appid` present | — | `appid` value (v1 token fallback) |
| J-06 | both `azp` and `appid` absent | — | `null` |

---

## 7. `AuthFlowResolver` unit tests

| # | JWT claims | Expected `AuthType` |
|---|---|---|
| R-01 | `scp` present and non-blank | `OBO` |
| R-02 | `scp` blank string | `CLIENT_CREDENTIALS` |
| R-03 | `scp` null / absent | `CLIENT_CREDENTIALS` |
| R-04 | JWT is null | `NONE` |

---

## 8. `PolicyEngine` unit tests

| # | Rules | Evaluator outcomes | Expected decision |
|---|---|---|---|
| E-01 | empty list | — | `DENY` |
| E-02 | one rule | `PERMIT` | `PERMIT` |
| E-03 | one rule | `DENY` | `DENY` |
| E-04 | two rules | `PERMIT`, `DENY` | `DENY` (short-circuit) |
| E-05 | two rules | `PERMIT`, `ABSTAIN` | `PERMIT` |
| E-06 | two rules | `ABSTAIN`, `ABSTAIN` | `PERMIT` (all-abstain fallback) |
| E-07 | rule with no matching evaluator | — | skipped → `PERMIT` (falls through to abstain default) |

---

## Summary matrix

```
                     │ PUBLIC │ SECURED / CC │ SECURED / OBO │ UNKNOWN ROUTE
─────────────────────┼────────┼──────────────┼───────────────┼──────────────
No token             │  200   │     401      │     401       │     403
Valid, allowed       │  200   │     200      │     200       │     403
Valid, wrong flow    │  200   │     403      │     403       │     403
Valid, not in policy │  200   │     403      │     403       │     403
Expired / malformed  │  200   │     401      │     401       │     401/403
```
