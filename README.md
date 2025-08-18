# Codex Webapp

This is a minimal demonstration of a Kotlin Spring Boot backend and Angular frontend.

## Backend
- Kotlin with Spring Boot, jOOQ and Liquibase
- Soft deletes for users
- Dockerfile for containerized build

## Frontend
- Angular with strong typing
- Simple login form and dashboard placeholder

## Running locally

Use Docker Compose:

```bash
docker-compose up --build
```

The backend will be on `http://localhost:8080` and the frontend on `http://localhost:4200`.
You can override the backend URL by setting the `API_URL` environment variable
for the frontend container. If unspecified it defaults to `http://localhost:8081`.

## Managing Environments in Kubernetes

For deployments beyond local development, you can manage separate environments (development, staging, production) using Kubernetes. See [KUBERNETES.md](KUBERNETES.md) for a basic overview and example structure. A sample Jenkins pipeline is provided in the [Jenkinsfile](Jenkinsfile) to automate builds and deployments.

### Deploying the backend with Skaffold

To build and deploy the backend to the `dev` namespace, a [Skaffold](https://skaffold.dev) configuration is available at `backend/skaffold.yaml`. With a Kubernetes cluster and registry reachable from your environment you can run:

```bash
skaffold run -f backend/skaffold.yaml --default-repo host.docker.internal:5000
```

This command builds the backend image, pushes it to the registry and applies the manifests in `kubernetes/dev` to create the namespace, database and backend deployment.

## Jenkins Build Environment

A Dockerfile is provided under `jenkins/` to build a Jenkins agent image with Node.js, the Docker CLI and `kubectl` installed. Build it with:

```bash
docker build -t codex-jenkins-agent ./jenkins
```

Use this image in your Jenkins setup to run the pipeline defined in `Jenkinsfile`.
