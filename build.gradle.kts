plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

val ktorVersion = "3.0.0"
val jacksonVersion = "2.18.0"

dependencies {
    runtimeOnly("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("org.apache.kafka:kafka-clients:3.8.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${jacksonVersion}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${jacksonVersion}")
    implementation("no.nav.common:audit-log:2.2023.01.10_13.49-81ddc732df3a")
    testImplementation("ch.qos.logback:logback-classic:1.5.10")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
}

group = "no.nav.arbeidsgiver"
version = "1.0-SNAPSHOT"
description = "consoleApp"
java.sourceCompatibility = JavaVersion.VERSION_17

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
