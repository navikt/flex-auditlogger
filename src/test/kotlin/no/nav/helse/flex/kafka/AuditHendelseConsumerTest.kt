@file:Suppress("ktlint:standard:max-line-length")

package no.nav.helse.flex.kafka

import no.nav.helse.flex.Application
import no.nav.helse.flex.utils.serialisertTilString
import org.amshove.kluent.`should contain`
import org.junit.jupiter.api.*
import org.mockito.Mockito.verify
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.time.Instant

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest(classes = [Application::class, KafkaConfig::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, printOnlyOnFailure = false)
@AutoConfigureObservability
class AuditHendelseConsumerTest {
    @Autowired
    private lateinit var redusertVenteperiodeConsumer: AuditHendelseConsumer

    @MockitoSpyBean
    private lateinit var auditLogger: Logger

    private val auditEntry =
        AuditEntry(
            appNavn = "flex-internal",
            utførtAv = "12345678910",
            oppslagPå = "10987654321",
            eventType = EventType.READ,
            forespørselTillatt = true,
            oppslagUtførtTid = Instant.now(),
            beskrivelse = "Henter alle sykepengesoknader",
            requestUrl = URI.create("/api/flex/sykepengesoknader"),
            requestMethod = "POST",
        )

    companion object {
        init {
            KafkaContainer(DockerImageName.parse("apache/kafka-native")).apply {
                start()
                System.setProperty("KAFKA_BROKERS", bootstrapServers)
            }
        }
    }

    @Test
    @Order(1)
    fun `Prosesserer auditEntry fra kafka`() {
        redusertVenteperiodeConsumer.prosesserKafkaMelding(
            auditEntry.serialisertTilString(),
        )

        val cefMessage = auditEntry.tilCEFFormat()

        verify(auditLogger).info(cefMessage)
        cefMessage `should contain`
            "CEF:0|Vedtaksløsning for sykepenger|flex-internal|1.0|audit:access|Sporingslogg|INFO|flexString1=Permit msg=Henter alle sykepengesoknader request=/api/flex/sykepengesoknader suid=12345678910 duid=10987654321 requestMethod=POST flexString1Label=Decision"
    }
}
