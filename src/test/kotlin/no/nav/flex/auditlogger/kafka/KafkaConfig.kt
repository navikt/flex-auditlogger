package no.nav.flex.auditlogger.kafka

import no.nav.flex.auditlogger.logger
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaConfig(
    @Value("\${KAFKA_BROKERS}") private val kafkaBrokers: String,
) {
    val log = logger()

    @Bean
    fun kafkaProducerForTest(): KafkaProducer<String, String> {
        val configs =
            mapOf(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.BATCH_SIZE_CONFIG to 100,
                ProducerConfig.ACKS_CONFIG to "all",
                ProducerConfig.RETRIES_CONFIG to 10,
                ProducerConfig.RETRY_BACKOFF_MS_CONFIG to 100,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaBrokers,
                SaslConfigs.SASL_MECHANISM to "PLAIN",
            )
        log.info("Kafka Producer Config: $configs")

        return KafkaProducer(configs)
    }
}
