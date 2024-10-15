FROM ghcr.io/navikt/baseimages/temurin:17
COPY /target/flex-auditlogger-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
