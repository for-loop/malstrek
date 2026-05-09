# ADR 0007: Java 17 and Gradle Build System

**Date:** 2025  
**Status:** Accepted  
**Deciders:** studio a priori

## Context

Malstrek is a real-time event processing application that must be reliable, maintainable, and performant. The language and build system must support:

- Reliable concurrent event processing
- Strong type safety
- Mature ecosystem of Kafka/Avro libraries
- Fast compilation and build times
- Cross-platform compatibility

### Options Considered

1. **Java 17 + Gradle** - Modern JVM language with latest LTS, flexible build tool
2. **Java 11 + Maven** - Older LTS, declarative build, broader ecosystem
3. **Go** - Fast execution, simple concurrency, but limited Kafka ecosystem
4. **Python** - Easy to write, but slower at runtime and weaker type system
5. **Kotlin + Gradle** - Modern JVM language with better syntax, smaller ecosystem

## Decision

**Use Java 17 (LTS) as the primary language and Gradle as the build tool.**

- **Language Version**: Java 17 (LTS release, supported until September 2026)
- **Build Tool**: Gradle 8.14.2
- **Build Features**: 
  - Shadow plugin for creating fat JARs with all dependencies
  - Gradle Wrapper for reproducible builds across environments
  - Avro plugin for code generation from `.avsc` schemas

## Rationale

### Java 17 Selection

- **Long-Term Support (LTS)**: Supported until September 2026; stability for production systems
- **Modern Features**: Records, pattern matching, sealed classes improve code clarity
- **Mature Ecosystem**: Kafka, Avro, Jackson, and all dependencies have excellent Java support
- **Performance**: Garbage collection improvements and optimizations since Java 11
- **Virtual Threads (Preview)**: Future capability for even better concurrency
- **Security**: Latest security patches and fixes in JDK distribution
- **Docker Support**: Excellent support in container environments
- **Team Familiarity**: Java expertise common in enterprise development

### Gradle Selection over Maven

- **Flexibility**: Gradle DSL allows complex build logic without XML verbosity
- **Performance**: Incremental compilation and caching are faster than Maven
- **Wrapper**: `gradlew` script ensures build consistency across machines
- **Shadow Plugin**: Excellent fat JAR support for containerized deployments
- **Configuration Cache**: Speeds up incremental builds significantly
- **Plugin Ecosystem**: Better Avro code generation support than Maven
- **Kotlin DSL**: `build.gradle.kts` provides IDE assistance and type checking

## Consequences

### Positive
- Kafka and Avro libraries are production-grade and widely used
- Mature debugging and profiling tools
- Excellent IDE support (IntelliJ, Eclipse, VS Code)
- JVM performance optimizations apply to long-running services
- Large community with ample documentation
- Gradle Wrapper ensures reproducible builds
- Easy to generate code from Avro schemas

### Negative
- JVM startup time (~2-5 seconds) adds initial deployment latency
- Base image size larger than Go or Python (~400MB with build tools, ~150MB runtime)
- Learning curve for team unfamiliar with Java or Gradle
- Gradle build files can become complex for large projects
- JVM memory overhead compared to compiled languages

## Build Configuration

### Key Dependencies

- **Kafka Client**: `org.apache.kafka:kafka-clients:3.3.1`
- **Avro**: `org.apache.avro:avro:1.11.3`
- **Confluent Schema Registry**: `io.confluent:kafka-avro-serializer:7.9.0`
- **Jackson**: `com.fasterxml.jackson.core:jackson-databind:2.15.2`
- **JUnit 5**: `5.12.1` for testing

### Build Targets

```gradle
// Debug build with full logging
./gradlew build --debug

// Release build (fat JAR)
./gradlew shadowJar

// Run tests
./gradlew test

// Code generation from Avro schemas
./gradlew generateAvroJava
```

### Toolchain Management

Java toolchain is explicitly configured to Java 17:

```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

This ensures:
- Gradle automatically downloads correct JDK if not available
- Build is reproducible across different machines
- No dependency on system Java version

## Deployment Implications

- **JVM Tuning**: Memory limits should be configured for container (typically -Xmx512m for small services)
- **Startup Time**: Plan for 5-10 second startup delay in orchestration
- **Image Size**: Base runtime image ~150MB, acceptable for most deployments
- **Observability**: Use standard JVM flags for profiling (`-XX:+PrintGCDetails`, etc.)

## Future Considerations

- **Project Loom (Virtual Threads)**: Java 21+ enables millions of concurrent threads; evaluate for high-concurrency scenarios
- **GraalVM Native Image**: Compile to native binary for faster startup (potential future optimization)
- **Project Panama (FFI)**: Call native libraries without JNI wrapper code
- **Java 21+ LTS**: Plan migration when released (September 2023); current LTS cycle ends September 2026

## Related Decisions

- ADR 0005: Containerization with Docker Multi-Stage Builds
