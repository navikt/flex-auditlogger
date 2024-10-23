package no.nav.helse.flex.kafka

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.flex.utils.objectMapper
import no.nav.helse.flex.utils.vaskFnr
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not contain`
import org.junit.jupiter.api.Test
import java.net.URI
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime

class JsonDeserialiseringTest {
    @Test
    fun `test vellykket deserialsering av kafkamelding med timestamp`() {
        val jsonMelding = """
            {"fagsystem":"tiltak",
             "appNavn":"tiltak-refusjon-api",
             "utførtAv":"Z992785",
             "oppslagPå":"24070178547",
             "eventType":"READ",
             "forespørselTillatt":true,
             "oppslagUtførtTid":1693222027.076186081,
             "beskrivelse":"Oppslag på korreksjoner",
             "requestUrl":"/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ",
             "requestMethod":"GET"
            }
        """
        val auditEntry =
            AuditEntry(
                fagsystem = "tiltak",
                appNavn = "tiltak-refusjon-api",
                utførtAv = "Z992785",
                oppslagPå = "24070178547",
                eventType = EventType.READ,
                forespørselTillatt = true,
                oppslagUtførtTid = Instant.ofEpochSecond(1693222027, 76186081),
                beskrivelse = "Oppslag på korreksjoner",
                requestUrl = URI.create("/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ"),
                requestMethod = "GET",
            )

        auditEntry `should be equal to` objectMapper.readValue<AuditEntry>(jsonMelding)
    }

    @Test
    fun `test vellykket deserialsering av kafkamelding med datostring`() {
        val jsonMelding = """
            {"fagsystem":"tiltak",
             "appNavn":"tiltak-refusjon-api",
             "utførtAv":"Z992785",
             "oppslagPå":"24070178547",
             "eventType":"READ",
             "forespørselTillatt":true,
             "oppslagUtførtTid":"2023-08-28T10:21:29.865897389Z",
             "beskrivelse":"Oppslag på korreksjoner",
             "requestUrl":"/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ",
             "requestMethod":"GET"
            }
        """
        val auditEntry =
            AuditEntry(
                fagsystem = "tiltak",
                appNavn = "tiltak-refusjon-api",
                utførtAv = "Z992785",
                oppslagPå = "24070178547",
                eventType = EventType.READ,
                forespørselTillatt = true,
                oppslagUtførtTid = ZonedDateTime.of(2023, 8, 28, 10, 21, 29, 865897389, ZoneOffset.UTC).toInstant(),
                beskrivelse = "Oppslag på korreksjoner",
                requestUrl = URI.create("/api/saksbehandler/korreksjon/01H8XVHYJWKBX71R87VDT80GGQ"),
                requestMethod = "GET",
            )

        auditEntry `should be equal to` objectMapper.readValue<AuditEntry>(jsonMelding)
    }

    @Test
    fun `test vask av fnr i feilmeldinger`() {
        val feilmeldingMedFnr = "Det gikk galt et sted med fnr: 16120101181"
        vaskFnr(feilmeldingMedFnr) `should not contain` feilmeldingMedFnr
        vaskFnr(feilmeldingMedFnr) `should contain` "[fnr]"
    }
}