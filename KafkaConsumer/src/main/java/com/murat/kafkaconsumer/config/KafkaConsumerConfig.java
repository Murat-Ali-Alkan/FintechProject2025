package com.murat.kafkaconsumer.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

/**
 * Kafka consumer configuration class for setting up Kafka listener containers and consumer factory.
 *
 * <p>This configuration enables the application to consume messages from Kafka topics
 * using the specified consumer properties such as broker address, group ID, and deserializers.</p>
 *
 * <p>It provides two beans:</p>
 * <ul>
 *   <li>{@code ConsumerFactory<String, String>} - Used to create Kafka consumers.</li>
 *   <li>{@code ConcurrentKafkaListenerContainerFactory<String, String>} - Used by Spring to create listener containers.</li>
 * </ul>
 *
 * <p>Messages are expected to have String keys and String values.</p>
 *
 */
@Configuration
public class KafkaConsumerConfig {

    /**
     * Creates a {@link ConsumerFactory} bean with configuration properties for connecting
     * to the Kafka broker and deserializing messages.
     *
     * @return a configured {@code ConsumerFactory} for String key/value pairs
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Kafka broker adresi
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        // Consumer group id
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "rate-group");
        // Mesajların deserializer ayarları
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);


        return new DefaultKafkaConsumerFactory<>(props);
    }


    /**
     * Creates a {@link ConcurrentKafkaListenerContainerFactory} bean that uses the configured
     * {@link ConsumerFactory} to listen to Kafka topics concurrently.
     *
     * @return a configured {@code ConcurrentKafkaListenerContainerFactory} for handling Kafka listeners
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
