package no.nav.helse.flex.kafka

import java.net.URI
import java.time.Instant

/**
 * @param utførtAv Nav-ident eller fnr på arbeidsgiver
 * @param oppslagPå Fnr på person det gjøres oppslag på, eller organisasjon
 * @param beskrivelse Beskrivelse av hva som er gjort, bør være "menneskelig lesbar"
 */
data class AuditEntry(
    val appNavn: String,
    val utførtAv: String,
    val oppslagPå: String,
    val eventType: EventType,
    val forespørselTillatt: Boolean,
    val oppslagUtførtTid: Instant,
    val beskrivelse: String,
    val requestUrl: URI,
    val requestMethod: String,
) {
    private val fagsystem = "Vedtaksløsning for sykepenger"

    fun tilCEFFormat(): String {
        return "CEF:0|${this.fagsystem}|${this.appNavn}|1.0|${eventType.logString}|" +
            "Sporingslogg|INFO|flexString1=${if (this.forespørselTillatt) "Permit" else "Deny"} " +
            "msg=${this.beskrivelse} request=${this.requestUrl} suid=${this.utførtAv} " +
            "duid=${this.oppslagPå} requestMethod=${this.requestMethod} flexString1Label=Decision " +
            "end=${this.oppslagUtførtTid.toEpochMilli()}"
    }
}

enum class EventType(val logString: String) {
    CREATE("audit:create"),
    READ("audit:access"),
    UPDATE("audit:update"),
    DELETE("audit:delete"),
}
