# shopbee-app

A Kubernetes application managed by Wi.

## Overview

This project bundles Kubernetes manifests for multiple services into a single deployable manifest.

## Quick Start

### 1. Add Services

Edit `settings.json` to add service artifact references:

```json
{
  "services": [
    {
      "artifact": "com.example:my-service:1.0.0",
      "port": 8080,
      "replicas": 1
    }
  ]
}
```

> Services must be released first (`wi release` in the service project) so their k8s manifests are available in the Maven repository.

### 2. Build

```bash
wi build
```

This resolves all service manifests and produces `dist/kubernetes/shopbee-app-kubernetes.yaml`.

### 3. Deploy

```bash
wi deploy
```

## Commands

| Command | Description |
|---------|-------------|
| `wi build` | Merge all manifests into a single YAML |
| `wi deploy` | Apply the merged manifest to the cluster |
| `wi undeploy` | Remove the deployment from the cluster |
| `wi redeploy` | Clean redeploy (undeploy + deploy) |
| `wi status` | Show deployment status |

## Project Structure

```
shopbee-app/
├── settings.json         # Service references
├── base/                 # Base K8s resources
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   └── ingress.yaml
└── dist/kubernetes/      # Build output
```

