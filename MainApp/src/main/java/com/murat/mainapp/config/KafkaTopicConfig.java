package com.murat.mainapp.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Kafka topic configuration class responsible for creating and registering Kafka topic
 * during application startup.
 * <p>
 * This configuration defines a {@link NewTopic} bean, which ensures the topic is created
 * if it does not already exist on the Kafka broker.
 * </p>
 *
 * @see NewTopic
 */
@Configuration
public class KafkaTopicConfig {

    /**
     * Defines a new Kafka topic named {@code test-topic} with 3 partitions and a replication factor of 1.
     *
     * @return a {@link NewTopic} instance.
     */
    @Bean
    public NewTopic newTopic() {
        return new NewTopic("test-topic", 3, (short) 1);
    }
}