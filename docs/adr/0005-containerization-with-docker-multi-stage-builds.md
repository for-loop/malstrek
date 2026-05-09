# ADR 0005: Containerization with Docker Multi-Stage Builds

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek consists of multiple components (Java application, migrations, databases, broker services) that must run reliably across development, testing, and production environments. The deployment strategy must:

- Ensure consistency across environments (dev = staging = prod)
- Minimize image sizes for faster deployment
- Support local development without external infrastructure
- Use industry-standard tools

### Options Considered

1. **Docker Multi-Stage Builds** - Single Dockerfile with multiple build stages, producing optimized images
2. **Docker Compose Only** - Use existing services (MariaDB, Metabase) without custom images
3. **Kubernetes** - Full orchestration platform, adds complexity for v1.0
4. **VM-based Deployment** - Manual VM provisioning, not reproducible
5. **Separate Dockerfiles** - One per component, harder to optimize

## Decision

**Adopt Docker with multi-stage builds for containerization.**

The Dockerfile includes three stages:

1. **migrate_tool** - golang-migrate binary for database migrations
2. **java_build** - Gradle build stage to compile application
3. **java_runner** - Final runtime image with only JRE and compiled JAR

## Rationale

- **Build Optimization**: Multi-stage approach keeps final image small (~300MB) by discarding build artifacts
- **Consistency**: Identical image runs in development (Docker Compose) and production
- **Efficiency**: Single Dockerfile is easier to maintain than multiple build configurations
- **Fast Rebuilds**: Layer caching means only changed stages rebuild
- **Security**: Final image contains only runtime dependencies, not build tools
- **Separation**: Each stage has a clear responsibility (build vs. runtime)

## Architecture

```
Dockerfile (3 stages)
├── migrate_tool (Alpine + golang-migrate)
├── java_build (Gradle + JDK 17)
└── java_runner (Eclipse Temurin JRE + JAR)

docker-compose.yml
├── db (MariaDB)
├── migrate (migrate_tool stage)
├── malstrek-app (java_runner stage)
├── metabase (Official image)
└── External: broker (Kafka/Zookeeper via streamer)
```

## Build Stages Explained

### Stage 1: migrate_tool
- Base: `alpine:3.22.2`
- Installs: golang-migrate binary
- Output: Standalone migrate executable
- Purpose: Database schema versioning

### Stage 2: java_build
- Base: `gradle:8.14.2-jdk17`
- Tasks: Compile source, run tests, create fat JAR with Shadow plugin
- Output: `app-all.jar` (bundled with all dependencies)
- Purpose: Compile-time stage, not included in final image

### Stage 3: java_runner
- Base: `eclipse-temurin:17-jre-jammy`
- Copies: Pre-built JAR from stage 2
- Size: ~150MB (JRE only, no build tools)
- Purpose: Production runtime

## Consequences

### Positive
- **Image Size**: 3-4x smaller than if build tools were included
- **Security**: No build tools in production container
- **Reproducibility**: Same Dockerfile works everywhere
- **Fast Iteration**: Gradle caching speeds up rebuilds
- **Single Source of Truth**: All build logic in one file
- **Easy Debugging**: Can inspect individual stages during development

### Negative
- **Build Time**: First build takes 5-10 minutes (downloads JDK, dependencies)
- **Docker Knowledge**: Team must understand multi-stage concepts
- **Limited Flexibility**: Adding new stages requires Dockerfile changes
- **Testing**: Each stage must be tested independently

## Docker Compose Integration

Development and local testing use Docker Compose:

```bash
docker compose build      # Build all images
docker compose up         # Start all services
docker compose down       # Stop and cleanup
```

Services auto-start in dependency order:
1. MariaDB database
2. Migration runner
3. Malstrek application
4. Metabase analytics

## Production Considerations

In production:
- Images are pushed to container registry (ECR, Docker Hub, etc.)
- Tag with version numbers: `malstrek:1.0.0`
- Orchestration via Kubernetes, ECS, or similar
- Environment variables injected at runtime via `.env` file or secrets manager

## Future Improvements

- Consider Alpine as base for JRE stage (~100MB smaller)
- Implement distroless images for maximum security
- Add health checks to detect startup failures
- Implement image scanning for vulnerabilities

## Related Decisions

- ADR 0004: Use golang-migrate for Database Schema Versioning
