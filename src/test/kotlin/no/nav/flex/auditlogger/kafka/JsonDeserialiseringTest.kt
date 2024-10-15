package no.nav.flex.auditlogger.kafka

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JsonDeserialiseringTest {
    val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    init {
        mapper.registerModule(JavaTimeModule())
    }

    @Test
    fun `test vellykket deserialsering av kafkamelding med timestamp`() {
        val jsonMelding = """
            {"appNavn":"tiltak-refusjon-api",
             "utførtAv":"Z992785",
             "oppslagPå":"24070178547",
             "eventType":"READ",
             "forespørselTillatt":true,
             "oppslagUtførtTid":1693222027.076186081,
             "beskrivelse":"Oppslag på korreksjoner",
             "requestUrl":"/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ",
             "requestMethod":"GET",
             "correlationId":"b9594bd4-58cb-4ec0-99ec-a261261b86d8"
            }
        """
        val auditEntry =
            AuditEntry(
                "tiltak-refusjon-api",
                "Z992785",
                "24070178547",
                EventType.READ,
                true,
                Instant.ofEpochSecond(1693222027, 76186081),
                "Oppslag på korreksjoner",
                URI.create("/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ"),
                "GET",
                "b9594bd4-58cb-4ec0-99ec-a261261b86d8",
            )

        assertEquals(auditEntry, mapper.readValue<AuditEntry>(jsonMelding))
    }

    @Test
    fun `test vellykket deserialsering av kafkamelding med datostring`() {
        val jsonMelding = """
            {"appNavn":"tiltak-refusjon-api",
             "utførtAv":"Z992785",
             "oppslagPå":"24070178547",
             "eventType":"READ",
             "forespørselTillatt":true,
             "oppslagUtførtTid":"2023-08-28T10:21:29.865897389Z",
             "beskrivelse":"Oppslag på korreksjoner",
             "requestUrl":"/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ",
             "requestMethod":"GET",
             "correlationId":"b9594bd4-58cb-4ec0-99ec-a261261b86d8"
            }
        """
        val auditEntry =
            AuditEntry(
                "tiltak-refusjon-api",
                "Z992785",
                "24070178547",
                EventType.READ,
                true,
                // "2023-08-28T10:21:29.865897389Z"
                ZonedDateTime.of(2023, 8, 28, 10, 21, 29, 865897389, ZoneOffset.UTC).toInstant(),
                "Oppslag på korreksjoner",
                URI.create("/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ"),
                "GET",
                "b9594bd4-58cb-4ec0-99ec-a261261b86d8",
            )

        assertEquals(auditEntry, mapper.readValue<AuditEntry>(jsonMelding))
    }

    @Test
    fun `test vask av fnr i feilmeldinger`() {
        val testFnr = "16120101181"
        try {
            mapper.readValue<AuditEntry>(
                """ {"deltakerFnr": "$testFnr"} """,
            )
            assertFalse("Deserialisering burde feile") { true }
        } catch (e: Exception) {
            assertFalse("Vasket feilmelding burde ikke inneholde fnr") { vaskFnr(e.message).contains(testFnr) }
            assertTrue("Vasket feilmelding inneholder maskert fnr") { vaskFnr(e.message).contains("\"1612*******\"") }
        }
    }
}
