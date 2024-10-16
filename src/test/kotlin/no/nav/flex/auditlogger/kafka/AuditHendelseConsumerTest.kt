@file:Suppress("ktlint:standard:max-line-length")

package no.nav.flex.auditlogger.kafka

import no.nav.common.audit_log.cef.AuthorizationDecision
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import no.nav.flex.auditlogger.Application
import no.nav.flex.auditlogger.utils.serialisertTilString
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.*
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.time.Instant
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@EnableMockOAuth2Server
@SpringBootTest(classes = [Application::class, KafkaConfig::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, printOnlyOnFailure = false)
@AutoConfigureObservability
class AuditHendelseConsumerTest {
    @Autowired
    private lateinit var redusertVenteperiodeConsumer: AuditHendelseConsumer

    @SpyBean
    private lateinit var auditLogger: AuditLogger

    private val kafkaMessage = auditEntryKafkaMelding()

    companion object {
        init {
            KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1")).apply {
                start()
                System.setProperty("KAFKA_BROKERS", bootstrapServers)
            }
        }
    }

    @Test
    @Order(1)
    fun `Sykmelding med redusert venteperiode lagres`() {
        redusertVenteperiodeConsumer.prosesserKafkaMelding(
            kafkaMessage.serialisertTilString(),
        )

        val auditEntry = kafkaMessage.auditEntry

        val cefMessage =
            CefMessage.builder()
                .applicationName("Flex")
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

        verify(auditLogger).log(cefMessage)
        cefMessage.toString() `should contain` "CEF:0|Flex|flex-internal-frontend|1.0|audit:access|Sporingslogg|INFO|flexString1=Permit msg=Sjekket søknaden til personen request=https://flex-internal.no duid=10987654321 requestMethod=GET flexString1Label=Decision"
    }

//    @Test
//    @Order(2)
//    fun `Kan lese inn samme sykmelding flere ganger`() {
//        redusertVenteperiodeConsumer.prosesserKafkaMelding(
//            sykmeldingId,
//            kafkaMessage.serialisertTilString(),
//        )
//
//        redusertVenteperiodeRepository.existsBySykmeldingId(sykmeldingId) shouldBeEqualTo true
//    }
//
//    @Test
//    @Order(3)
//    fun `Fjernes ved tombstone event`() {
//        redusertVenteperiodeConsumer.prosesserKafkaMelding(
//            sykmeldingId,
//            null,
//        )
//
//        redusertVenteperiodeRepository.existsBySykmeldingId(sykmeldingId) shouldBeEqualTo false
//    }

    private fun auditEntryKafkaMelding(): AuditEntryKafkaMelding {
        return AuditEntryKafkaMelding(
            auditEntry =
                AuditEntry(
                    appNavn = "flex-internal-frontend",
                    utførtAv = "12345678910",
                    oppslagPå = "10987654321",
                    eventType = EventType.READ,
                    forespørselTillatt = true,
                    oppslagUtførtTid = Instant.now(),
                    beskrivelse = "Sjekket søknaden til personen",
                    requestUrl = URI.create("https://flex-internal.no"),
                    requestMethod = "GET",
                    correlationId = UUID.randomUUID().toString(),
                ),
        )
    }
}

data class AuditEntryKafkaMelding(
    val auditEntry: AuditEntry,
)
