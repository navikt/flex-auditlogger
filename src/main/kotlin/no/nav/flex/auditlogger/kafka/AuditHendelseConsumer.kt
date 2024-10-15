package no.nav.flex.auditlogger.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import no.nav.common.audit_log.cef.AuthorizationDecision
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.audit_log.log.AuditLoggerImpl
import no.nav.flex.auditlogger.utils.log
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import java.time.Duration

class AuditHendelseConsumer(
    private val consumer: Consumer<String, String>,
) {
    val auditLogger: AuditLogger = AuditLoggerImpl()
    val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun start() =
        runBlocking {
            log.info("Starter konsumering på topic: ${Topics.AUDIT_HENDELSE}")
            consumer.subscribe(listOf(Topics.AUDIT_HENDELSE))
            mapper.registerModule(JavaTimeModule())
            while (true) {
                val records: ConsumerRecords<String, String> = consumer.poll(Duration.ofSeconds(5))
                records.isEmpty && continue
                records.forEach {
                    try {
                        val melding: AuditEntry = mapper.readValue(it.value())

                        val cefMessage =
                            CefMessage.builder()
                                .applicationName("Flex")
                                .loggerName(melding.appNavn)
                                .event(cefEvent(melding.eventType))
                                .name("Sporingslogg")
                                .severity(CefMessageSeverity.INFO)
                                .authorizationDecision(
                                    if (melding.forespørselTillatt) AuthorizationDecision.PERMIT else AuthorizationDecision.DENY,
                                ) // Bruk AuthorizationDecision.DENY hvis Nav-ansatt ikke fikk tilgang til å gjøre oppslag
                                .sourceUserId(melding.utførtAv)
                                .destinationUserId(melding.oppslagPå)
                                .timeEnded(melding.oppslagUtførtTid.toEpochMilli())
                                .extension("msg", melding.beskrivelse)
                                .extension("request", melding.requestUrl.toString())
                                .extension("requestMethod", melding.requestMethod)
                                .extension("dproc", melding.correlationId)
                                .build()

                        auditLogger.log(cefMessage)
                    } catch (ex: Exception) {
                        log.error("Kunne ikke logge audit-hendelse: {}", vaskFnr(ex.message))
                    }
                    consumer.commitAsync()
                }
            }
        }
}

fun cefEvent(e: EventType) =
    when (e) {
        EventType.CREATE -> CefMessageEvent.CREATE
        EventType.READ -> CefMessageEvent.ACCESS
        EventType.UPDATE -> CefMessageEvent.UPDATE
        EventType.DELETE -> CefMessageEvent.DELETE
    }

val fnrStringRegex = Regex("\\d{11}") // Regex to match exactly 11 digits

fun vaskFnr(message: String?): String {
    return message?.replace(fnrStringRegex, "[fnr]") ?: ""
}
