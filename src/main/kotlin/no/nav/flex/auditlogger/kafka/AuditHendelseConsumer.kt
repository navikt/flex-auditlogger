package no.nav.flex.auditlogger.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.common.audit_log.cef.AuthorizationDecision
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import no.nav.flex.auditlogger.logger
import no.nav.flex.auditlogger.utils.objectMapper
import no.nav.flex.auditlogger.utils.vaskFnr
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val AUDIT_TOPIC = "flex.auditlogging"

@Component
class AuditHendelseConsumer(
    private val auditLogger: AuditLogger,
) {
    val log = logger()

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
        prosesserKafkaMelding(cr.value())
        acknowledgment.acknowledge()
    }

    fun prosesserKafkaMelding(auditEntryKafkaMelding: String) {
        try {
            val auditEntry: AuditEntry = objectMapper.readValue<AuditEntry>(auditEntryKafkaMelding)
            val cefMessage =
                CefMessage.builder()
                    .applicationName(auditEntry.fagsystem)
                    .loggerName(auditEntry.appNavn)
                    .event(cefEvent(auditEntry.eventType))
                    .name("Sporingslogg")
                    .severity(CefMessageSeverity.INFO)
                    .authorizationDecision(
                        // Bruk AuthorizationDecision.DENY hvis Nav-ansatt ikke fikk tilgang til å gjøre oppslag
                        if (auditEntry.forespørselTillatt) AuthorizationDecision.PERMIT else AuthorizationDecision.DENY,
                    )
                    .sourceUserId(auditEntry.utførtAv)
                    .destinationUserId(auditEntry.oppslagPå)
                    .timeEnded(auditEntry.oppslagUtførtTid.toEpochMilli())
                    .extension("msg", auditEntry.beskrivelse)
                    .extension("request", auditEntry.requestUrl.toString())
                    .extension("requestMethod", auditEntry.requestMethod)
                    .extension("dproc", auditEntry.correlationId)
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
