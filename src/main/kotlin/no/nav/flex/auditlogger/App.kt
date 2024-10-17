package no.nav.flex.auditlogger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    // Lettuce-spring boot interaksjon. Se https://github.com/lettuce-io/lettuce-core/issues/1767
    System.setProperty("io.lettuce.core.jfr", "false")
    runApplication<Application>(*args)
}
