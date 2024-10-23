package no.nav.helse.flex.utils

val fnrStringRegex = Regex("\\d{11}") // Regex to match exactly 11 digits

fun vaskFnr(message: String?): String {
    return message?.replace(fnrStringRegex, "[fnr]") ?: ""
}
