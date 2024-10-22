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
import org.springframework.boot.test.mock.mockito.SpyBean
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.net.URI
import java.time.Instant
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@SpringBootTest(classes = [Application::class, KafkaConfig::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, printOnlyOnFailure = false)
@AutoConfigureObservability
class AuditHendelseConsumerTest {
    @Autowired
    private lateinit var redusertVenteperiodeConsumer: AuditHendelseConsumer

    @SpyBean
    private lateinit var auditLogger: Logger

    private val auditEntry =
        AuditEntry(
            fagsystem = "Sykepenger",
            appNavn = "flex-internal-frontend",
            utførtAv = "12345678910",
            oppslagPå = "10987654321",
            eventType = EventType.READ,
            forespørselTillatt = true,
            oppslagUtførtTid = Instant.now(),
            beskrivelse = "Sjekket søknaden til personen",
            requestUrl = URI.create("https://flex-internal.no"),
            requestMethod = "GET",
        )

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
            auditEntry.serialisertTilString(),
        )

        val cefMessage = auditEntry.tilCEFFormat()

        verify(auditLogger).info(cefMessage)
        cefMessage `should contain`
            """
            CEF:0|Sykepenger|flex-internal-frontend|1.0|audit:access|Sporingslogg|INFO|
            flexString1=Permit 
            msg=Sjekket søknaden til personen 
            request=https://flex-internal.no 
            suid=12345678910 
            duid=10987654321 
            requestMethod=GET 
            flexString1Label=Decision
            """.trimIndent()
    }
}
