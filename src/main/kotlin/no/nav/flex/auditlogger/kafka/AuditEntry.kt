package no.nav.flex.auditlogger.kafka

import java.net.URI
import java.time.Instant

data class AuditEntry(
    val appNavn: String,
    // Nav-ident eller fnr på arbeidsgiver
    val utførtAv: String,
    // Fnr på person det gjøres oppslag på, eller organisasjon
    val oppslagPå: String,
    val eventType: EventType,
    val forespørselTillatt: Boolean,
    val oppslagUtførtTid: Instant,
    // Beskrivelse av hva som er gjort, bør være "menneskelig lesbar"
    val beskrivelse: String,
    val requestUrl: URI,
    val requestMethod: String,
    val correlationId: String,
)
