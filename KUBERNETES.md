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




start "" /min kubectl proxy --address=0.0.0.0 --port=8001
kubectl -n kubernetes-dashboard create token dashboard-admin

eyJhbGciOiJSUzI1NiIsImtpZCI6IjBUM2ZtZTJrU2lDREdLZERUdEppcngxekQwR3JlX003QWc1bU1tNFcyZjgifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjLmNsdXN0ZXIubG9jYWwiXSwiZXhwIjoxNzQ5NjExMzcwLCJpYXQiOjE3NDk2MDc3NzAsImlzcyI6Imh0dHBzOi8va3ViZXJuZXRlcy5kZWZhdWx0LnN2Yy5jbHVzdGVyLmxvY2FsIiwianRpIjoiODQyOWM1ZWUtMzQyNC00MDVmLWJhZGQtNmNiN2NlMzExMGE0Iiwia3ViZXJuZXRlcy5pbyI6eyJuYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsInNlcnZpY2VhY2NvdW50Ijp7Im5hbWUiOiJkYXNoYm9hcmQtYWRtaW4iLCJ1aWQiOiI0ODVhZmU5Mi0zNmEyLTRiMmUtYTY5Mi1lYTY1Y2RlZTMxNzMifX0sIm5iZiI6MTc0OTYwNzc3MCwic3ViIjoic3lzdGVtOnNlcnZpY2VhY2NvdW50Omt1YmVybmV0ZXMtZGFzaGJvYXJkOmRhc2hib2FyZC1hZG1pbiJ9.Ne4uZaZvd8rdw6gDNewVPI0h09L9wZv4Q14OHXDanq_LQWwzB2fvYXqXtdNFKsnkns1aWvuZ2KOdkl8tKBvE6yTRt-JF3XOQfr1bQQ66cN3kO8LXIP8gPE6v5nD8bBHDTt_74TvGz6Fa3PyJAzWWukn6i6nrmG9BZFMJaBxGQkSE28kZJPk6tjdjJSFBxrKzwaPe2OY2LtFCznT6TOCF-pbWBXSSZyeB8KtK6qOayYJ4dGbj6tBSqcjbWJQT6_cNMxQOyYFSrLFpV1QN0_1YtwlVBfug8x9ofItWjC6tgCL4ZDcqfgBfQ6N8gg7wKX_MOw2T5Ii_mfQAD7aYdIItBw
