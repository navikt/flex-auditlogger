package no.nav.flex.auditlogger.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.common.audit_log.cef.AuthorizationDecision
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.audit_log.log.AuditLoggerImpl
import no.nav.flex.auditlogger.logger
import no.nav.flex.auditlogger.utils.objectMapper
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val AUDIT_TOPIC = "flex.auditlogging"

@Component
class AuditHendelseConsumer {
    val log = logger()
    val auditLogger: AuditLogger = AuditLoggerImpl()

    @KafkaListener(
        topics = [AUDIT_TOPIC],
        containerFactory = "aivenKafkaListenerContainerFactory",
        properties = ["auto.offset.reset = latest"],
        id = "flex-auditlogging",
        idIsGroup = false,
    )
    fun listen(
        cr: ConsumerRecord<String, String>,
        acknowledgment: Acknowledgment,
    ) {
        log.info("Starter konsumering på topic: $AUDIT_TOPIC")
        try {
            val melding: AuditEntry = objectMapper.readValue(cr.value())

            val cefMessage =
                CefMessage.builder()
                    .applicationName("Flex")
                    .loggerName(melding.appNavn)
                    .event(cefEvent(melding.eventType))
                    .name("Sporingslogg")
                    .severity(CefMessageSeverity.INFO)
                    .authorizationDecision(
                        // Bruk AuthorizationDecision.DENY hvis Nav-ansatt ikke fikk tilgang til å gjøre oppslag
                        if (melding.forespørselTillatt) AuthorizationDecision.PERMIT else AuthorizationDecision.DENY,
                    )
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
