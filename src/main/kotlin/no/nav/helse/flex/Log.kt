package no.nav.helse.flex

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

inline fun <reified T> T.logger(): Logger = LoggerFactory.getLogger(T::class.java)

@Configuration
class AuditLoggerConfig {
    @Bean
    fun auditLogger(): Logger = LoggerFactory.getLogger("auditLogger")
}
