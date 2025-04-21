package com.murat.kafkaconsumeropensearch.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;



/**
 * Service class responsible for consuming and logging Kafka messages.
 *
 * <p>This service listens to messages from the Kafka topic {@code "test-topic"}
 * using the consumer group {@code "rate-group-2"} and logs each message using
 * a custom logger named {@code "KafkaLogger"}.</p>
 *
 * <p>Logging is handled via Log4j's {@link org.apache.logging.log4j.Logger}
 * to allow centralized and configurable message tracking.</p>
 *
 */
@Service
public class KafkaConsumerService {

    /**
     * Custom logger instance used to log Kafka messages under the name "KafkaLogger".
     */
    private static final Logger kafkaLogger = LogManager.getLogger("KafkaLogger");


    /**
     * Kafka listener method that is triggered when a new message is received
     * from the {@code "test-topic"}.
     *
     * <p>The message is logged via the custom {@code KafkaLogger} at {@code INFO} level.</p>
     *
     * @param message the raw message received from the Kafka topic
     */
    @KafkaListener(topics = "test-topic", groupId = "rate-group-2")
    public void listen(String message) {
        kafkaLogger.info(message);
    }
}
