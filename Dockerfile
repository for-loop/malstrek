# Migration tool for MariaDB using golang-migrate
FROM alpine:3.22.2 AS migrate_tool

RUN apk update && apk --no-cache add curl unzip

ARG MIGRATE_VERSION=4.18.3
RUN curl -L "https://github.com/golang-migrate/migrate/releases/download/v${MIGRATE_VERSION}/migrate.linux-arm64.tar.gz" | tar xvz

RUN mv /migrate /usr/local/bin/migrate

COPY ./migrations /migrations

COPY ./migrate/wait-for-mariadb.sh /usr/local/bin/wait-for-mariadb
RUN chmod +x /usr/local/bin/wait-for-mariadb

ENTRYPOINT ["sh", "/usr/local/bin/wait-for-mariadb", "db", "3306"]
CMD ["sh"]


# Malstrek app
FROM gradle:8.14.2-jdk17 AS java_build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY app/build.gradle.kts .
COPY settings.gradle.kts .
COPY app/src src
RUN ./gradlew build --no-daemon --console=plain

FROM eclipse-temurin:17-jre-jammy AS java_runner
WORKDIR /app

COPY --from=java_build ./app/build/libs/*-all.jar app.jar

CMD ["java", "-jar", "app.jar"]