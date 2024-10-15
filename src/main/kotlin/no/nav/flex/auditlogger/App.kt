package no.nav.flex.auditlogger

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import mu.KotlinLogging
import no.nav.flex.auditlogger.kafka.AuditHendelseConsumer
import no.nav.flex.auditlogger.kafka.properties
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.io.Closeable

class App(private val auditHendelseConsumer: AuditHendelseConsumer) : Closeable {
    private val logger = KotlinLogging.logger {}
    private val server =
        embeddedServer(Netty, port = 8092) {

            routing {
                get("/internal/isAlive") { call.respond(HttpStatusCode.OK) }
                get("/internal/isReady") { call.respond(HttpStatusCode.OK) }
            }
        }

    fun start() {
        logger.info("Starter applikasjon :)")
        server.start()
        auditHendelseConsumer.start()
    }

    override fun close() {
        logger.info("Stopper app")
        server.stop(0, 0)
    }
}

fun main() {
    // Setup kafka and database
    val consumer: Consumer<String, String> = KafkaConsumer(properties())
    val auditHendelseConsumer = AuditHendelseConsumer(consumer)

    App(auditHendelseConsumer).start()
}
