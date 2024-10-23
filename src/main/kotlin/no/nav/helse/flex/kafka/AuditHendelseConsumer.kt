package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.logger
import no.nav.helse.flex.utils.objectMapper
import no.nav.helse.flex.utils.vaskFnr
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

const val AUDIT_TOPIC = "flex.auditlogging"

@Component
class AuditHendelseConsumer(
    private val auditLogger: Logger,
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
        prosesserKafkaMelding(cr.value())
        acknowledgment.acknowledge()
    }

    fun prosesserKafkaMelding(auditEntryKafkaMelding: String) {
        try {
            val auditEntry: AuditEntry = objectMapper.readValue<AuditEntry>(auditEntryKafkaMelding)
            auditLogger.info(auditEntry.tilCEFFormat())
        } catch (ex: Exception) {
            log.error("Kunne ikke logge audit-hendelse: {}", vaskFnr(ex.message))
        }
    }
}
