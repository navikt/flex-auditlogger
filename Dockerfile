FROM ghcr.io/navikt/baseimages/temurin:17
COPY /target/tiltak-auditlogger-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
