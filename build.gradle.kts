plugins {
    id("java")
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    kotlin("jvm") version "2.0.20"
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }

    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }
}

val ktorVersion = "3.0.0"
val jacksonVersion = "2.18.0"
val auditLogVersion = "3.2023.09.13_04.55-a8ff452fbd94"

dependencies {
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("no.nav.common:audit-log:$auditLogVersion")
    testImplementation("ch.qos.logback:logback-classic:1.5.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
}

group = "no.nav.helse.flex"
version = properties["version"] ?: "local-build"
description = "consoleApp"
java.sourceCompatibility = JavaVersion.VERSION_21

publishing {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/navikt/sykepengesoknad-kafka")
            credentials {
                username = System.getenv("GITHUB_USERNAME")
                password = System.getenv("GITHUB_PASSWORD")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {

            pom {
                name.set("sykepengesoknad-kafka")
                url.set("https://github.com/navikt/sykepengesoknad-kafka")
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:https://github.com/navikt/sykepengesoknad-kafka.git")
                    developerConnection.set("scm:git:https://github.com/navikt/sykepengesoknad-kafka.git")
                    url.set("https://github.com/navikt/sykepengesoknad-kafka")
                }
            }
            from(components["java"])
        }
    }
}
