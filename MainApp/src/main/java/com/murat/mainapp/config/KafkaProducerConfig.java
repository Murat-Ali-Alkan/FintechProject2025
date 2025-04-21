package com.murat.mainapp.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up Kafka producer components.
 *
 * <p>
 * This class defines beans necessary for producing messages to Kafka topics,
 * including the producer factory and Kafka template.
 * </p>
 *
 * <p>
 * It uses basic String serialization for both keys and values and sets up a connection
 * to a Kafka broker defined by the {@code bootstrap.servers} configuration.
 * </p>
 */
@Configuration
public class KafkaProducerConfig {

    /**
     * Creates a {@link ProducerFactory} that sets up configuration for Kafka producers.
     * <p>
     * This factory uses String serializers for both keys and values and connects to a Kafka broker
     * running at {@code localhost:9092}.
     * </p>
     *
     * @return a configured {@link ProducerFactory} instance
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * Creates a KafkaTemplate bean used to send messages to Kafka topics.
     *
     * @return a {@link KafkaTemplate} instance configured with the producer factory
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}