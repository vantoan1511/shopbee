# Shopbee Keycloak

This repository contains the Keycloak configuration and custom themes for the Shopbee identity and access management system.

## Version
The current version of this repository is tracked in the [VERSION](file:///d:/Projects/shopbee/shopbee-keycloak/VERSION) file.

## Features
* **Custom Shopbee Theme**: A modern login experience designed to match the Shopbee brand aesthetic, including:
  * Dark glassmorphism card design
  * Glowing gradient accents
  * Typography utilizing Inter and Outfit fonts
  * Custom branding logo
* **Docker Packaging**: Two-stage optimized [Dockerfile](file:///d:/Projects/shopbee/shopbee-keycloak/Dockerfile) building a ready-to-run Keycloak distribution.
* **Kubernetes Orchestration**: Pre-configured StatefulSet and Service manifests in [shopbee-keycloak.yaml](file:///d:/Projects/shopbee/shopbee-keycloak/shopbee-keycloak.yaml) to run Keycloak on Kubernetes (e.g. Rancher Desktop).

---

## Local Development & Theme Hot Reload

To build and style the theme interactively without restarting the container on every CSS or HTML template change, you must mount the local theme files and disable Keycloak's theme cache.

### 1. Disable Cache & Run via Docker (CLI)

Run the following command from the repository root:

#### On PowerShell (Windows):
```powershell
docker run --name shopbee-keycloak-dev `
  -p 8080:8080 `
  -v "${PWD}/themes:/opt/keycloak/themes" `
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin `
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin `
  quay.io/keycloak/keycloak:26.5.4 `
  start-dev
```

#### On Bash (Linux/macOS):
```bash
docker run --name shopbee-keycloak-dev \
  -p 8080:8080 \
  -v "$(pwd)/themes:/opt/keycloak/themes" \
  -e KC_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:26.5.4 \
  start-dev
```

> [!NOTE]
> * **`start-dev`**: Starts Keycloak in development mode, which automatically disables the theme caches.
> * **Volume Mount**: We mount the local `themes/` directory directly into `/opt/keycloak/themes`.

---

### 2. Run via Docker Compose

Alternatively, you can use Docker Compose to run Keycloak locally. Create a `docker-compose.yaml` in the root:

```yaml
version: '3.8'

services:
  keycloak:
    image: quay.io/keycloak/keycloak:26.5.4
    container_name: shopbee-keycloak-dev
    ports:
      - "8080:8080"
    environment:
      - KC_BOOTSTRAP_ADMIN_USERNAME=admin
      - KC_BOOTSTRAP_ADMIN_PASSWORD=admin
      # Explicitly turn off caches (useful if not using start-dev)
      - KC_THEME_CACHE_TEMPLATES: "false"
      - KC_THEME_CACHE_THEMES: "false"
    volumes:
      # Mount the local themes folder
      - ./themes:/opt/keycloak/themes
    command: start-dev
```

Then start the container:
```bash
docker compose up -d
```

---

## Production Build

To build the production-ready Docker image containing the packaged theme:

```bash
docker build -t shopbee-keycloak:latest .
```
