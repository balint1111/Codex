# Managing Environments in Kubernetes

This project currently uses Docker Compose for local development. To manage different environments (development, staging, production) in Kubernetes, you can organize resources using namespaces or separate clusters.

## Example Setup

1. **Namespaces**: Create a namespace for each environment.
   ```bash
   kubectl create namespace codex-dev
   kubectl create namespace codex-staging
   kubectl create namespace codex-prod
   ```
   Deploy the backend and frontend to the appropriate namespace. Use separate configuration files (e.g., `deployment-dev.yaml`, `deployment-staging.yaml`, `deployment-prod.yaml`) that reference the correct Docker images and environment variables.

2. **Helm**: Use Helm charts to manage templated configurations across environments. Values files (`values-dev.yaml`, `values-staging.yaml`, `values-prod.yaml`) let you customize settings per environment while reusing the same chart structure.

3. **CI/CD Integration**: Configure your CI/CD pipeline to build Docker images and deploy them using `kubectl` or Helm for each environment. Tools like GitHub Actions, GitLab CI, or Jenkins can orchestrate these steps.

## Recommended Directory Structure

```
kubernetes/
├── dev/
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   └── ...
├── staging/
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   └── ...
└── prod/
    ├── backend-deployment.yaml
    ├── frontend-deployment.yaml
    └── ...
```

Each folder contains the Kubernetes manifests or Helm values specific to that environment.

## Jenkins Pipeline

A simple [Jenkinsfile](Jenkinsfile) is included to build Docker images, push them to a
registry, and deploy the manifests from the `kubernetes/` directory. The pipeline
runs tests, builds the frontend and backend containers, and applies the files for the
`dev`, `staging`, and `prod` namespaces.

Refer to the official [Kubernetes documentation](https://kubernetes.io/docs/home/) for additional best practices.

## Skaffold

For a streamlined developer workflow, you can use [Skaffold](https://skaffold.dev) to build images and deploy the `dev` environment manifests.

### Backend

```bash

skaffold run -f backend/skaffold.yaml --default-repo host.docker.internal:5000
```

### Frontend

```bash

skaffold run -f frontend/skaffold.yaml --default-repo host.docker.internal:5000
```

These commands mirror the `dev` deployment performed in the Jenkins pipeline.

## start kubernetes frontend
start "" /min kubectl proxy --address=0.0.0.0 --port=8001
kubectl -n kubernetes-dashboard create token dashboard-admin
http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/#/login

## Keycloak Operator and OIDC Login

The `dev` environment uses the [Keycloak Operator](https://www.keycloak.org/operator) to provide a Keycloak instance for
OIDC authentication. The manifest at `kubernetes/dev/keycloak.yaml` creates the Keycloak server, a `codex` realm and a public
client used by the frontend.

The backend reads the issuer URL from the `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI` environment variable
configured in `kubernetes/dev/backend-deployment.yaml`. The frontend receives the Keycloak settings via environment
variables defined in `kubernetes/dev/frontend-deployment.yaml`.
