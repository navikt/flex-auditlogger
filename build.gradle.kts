import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.0.21"
}

group = "no.nav.helse.flex"
version = "1"
description = "flex-auditlogging"
java.sourceCompatibility = JavaVersion.VERSION_21

ext["okhttp3.version"] = "4.12" // Token-support tester trenger MockWebServer.

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

val jacksonVersion = "2.18.2"
val auditLogVersion = "3.2023.09.13_04.55-a8ff452fbd94"
val kluentVersion = "1.73"
val testContainersVersion = "1.20.4"

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation(kotlin("stdlib"))
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.papertrailapp:logback-syslog4j:1.0.0")

    // spring
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("ch.qos.logback:logback-classic:1.5.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.0")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("org.testcontainers:kafka")
    testImplementation(platform("org.testcontainers:testcontainers-bom:$testContainersVersion"))
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
