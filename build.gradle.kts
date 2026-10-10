import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    kotlin("jvm") version "2.4.10"
    kotlin("plugin.spring") version "2.4.10"
}

group = "no.nav.helse.flex"
version = "1"
description = "flex-auditlogging"
java.sourceCompatibility = JavaVersion.VERSION_21

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

val auditLogVersion = "3.2023.09.13_04.55-a8ff452fbd94"
val kluentVersion = "1.73"
val testContainersVersion = "2.0.5"
val mockWebServerVersion = "5.5.0"
val logstashLogbackEncoderVersion = "9.0"

dependencies {

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("tools.jackson.module:jackson-module-kotlin")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-micrometer-metrics-test")
    testImplementation("org.springframework.boot:spring-boot-micrometer-tracing-test")
    testImplementation("com.squareup.okhttp3:mockwebserver3:$mockWebServerVersion")
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:testcontainers-kafka:$testContainersVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.add("-Xjsr305=strict")
        if (System.getenv("CI") == "true") {
            allWarningsAsErrors.set(true)
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs("-XX:+EnableDynamicAgentLoading")
        testLogging {
            events("PASSED", "FAILED", "SKIPPED")
            exceptionFormat = FULL
        }
        failFast = false
        reports.html.required.set(false)
        reports.junitXml.required.set(false)
        maxParallelForks =
            if (System.getenv("CI") == "true") {
                (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1).coerceAtMost(4)
            } else {
                2
            }
    }
}

tasks {
    bootJar {
        archiveFileName = "app.jar"
    }
}
