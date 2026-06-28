# Implementation Plan: TICKET-101 Shopbee App Deployment Customizations

This service-specific plan details the manifest and realm importing changes needed within the `shopbee-app` platform deployment configuration repository.

## Proposed Changes

### 1. Keycloak Realm Setup

#### [MODIFY] [realm-shopbee.json](file:///D:/Projects/shopbee/shopbee-app/realm-shopbee.json)
Configure the imported default realm to use the newly created `shopbee` theme by setting the login theme value in the root configuration object:
```json
"loginTheme": "shopbee",
```

---

### 2. Platform Deployment Manifests

#### [MODIFY] [dist/deployments/shopbee-keycloak.yaml](file:///D:/Projects/shopbee/shopbee-app/dist/deployments/shopbee-keycloak.yaml)
Coordinate with the custom built Keycloak IAM container image:
- Modify the container image mapping:
  - From: `image: quay.io/keycloak/keycloak:26.5.4`
  - To: `image: shopbee-keycloak:latest` (or target container registry path)

---

## Verification & Testing Plan

### 1. JSON Schema Validation
- Validate the JSON format of `realm-shopbee.json` to ensure syntax is clean:
  ```bash
  jq empty realm-shopbee.json
  ```

### 2. Manifest Validation
- Run dry-run validation checks on the Kubernetes manifests using `kubectl`:
  ```bash
  kubectl apply --dry-run=client -f dist/deployments/shopbee-keycloak.yaml
  ```
