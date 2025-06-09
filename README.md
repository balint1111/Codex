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
