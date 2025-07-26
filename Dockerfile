FROM alpine:latest

RUN apk update && apk --no-cache add curl unzip

ARG MIGRATE_VERSION=4.18.3
RUN curl -L "https://github.com/golang-migrate/migrate/releases/download/v${MIGRATE_VERSION}/migrate.linux-arm64.tar.gz" | tar xvz

RUN mv /migrate /usr/local/bin/migrate

COPY ./migrations /migrations

COPY ./migrate/wait-for-mariadb.sh /usr/local/bin/wait-for-mariadb
RUN chmod +x /usr/local/bin/wait-for-mariadb

ENTRYPOINT ["sh", "/usr/local/bin/wait-for-mariadb", "db", "3306"]
CMD ["sh"]
