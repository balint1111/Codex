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




start "" /min kubectl proxy --address=0.0.0.0 --port=8080
kubectl -n kubernetes-dashboard create token dashboard-admin
