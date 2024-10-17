package no.nav.flex.auditlogger

import no.nav.common.audit_log.log.AuditLogger
import no.nav.common.audit_log.log.AuditLoggerImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

inline fun <reified T> T.logger(): Logger {
    return LoggerFactory.getLogger(T::class.java)
}

@Configuration
class Log {
    @Bean
    fun auditLogger(): AuditLogger {
        return AuditLoggerImpl() // Instantiate the implementation of AuditLogger
    }
}
