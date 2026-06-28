# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-28

### Added
- **Custom Keycloak Theme (`shopbee`)**:
  - Implemented custom brand aesthetics including dark glassmorphism card designs, typography, layout fixes, and glowing gradients.
  - Added CSS custom property overrides for PatternFly components (input groups, buttons, helper text colors).
  - Added brand logo asset (`themes/shopbee/login/resources/img/logo.png`).
  - Added theme properties configuration (`themes/shopbee/login/theme.properties`) inheriting from `keycloak`.
- **Dockerization**:
  - Created a two-stage optimized [Dockerfile](file:///d:/Projects/shopbee/shopbee-keycloak/Dockerfile) with theme baking and build-time optimization (`kc.sh build`).
- **Kubernetes Manifests**:
  - Configured stateful deployment options in [shopbee-keycloak.yaml](file:///d:/Projects/shopbee/shopbee-keycloak/shopbee-keycloak.yaml) including theme mount volume templates for local development.
- **Realm Configuration**:
  - Synced realm configurations in [realm-shopbee.json](file:///d:/Projects/shopbee/shopbee-keycloak/realm-shopbee.json) and [shopbee-keycloak-realm-configs.yaml](file:///d:/Projects/shopbee/shopbee-keycloak/shopbee-keycloak-realm-configs.yaml).
- **Documentation**:
  - Created [README.md](file:///d:/Projects/shopbee/shopbee-keycloak/README.md) with guidelines for local theme hot reloading.
  - Created [VERSION](file:///d:/Projects/shopbee/shopbee-keycloak/VERSION) file.
