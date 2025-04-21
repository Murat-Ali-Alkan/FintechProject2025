package com.murat.mainapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for sending messages to test-topic.
 * <p>
 * This service provides functionality to send messages to a predefined Kafka topic named "test-topic" using the
 * {@link KafkaTemplate}. It abstracts the communication with Kafka from other layers of the application.
 * </p>
 */
@Service
public class KafkaProducerService {
    /**
     * The name of the Kafka topic to which messages will be sent.
     */
    private static final String TOPIC = "test-topic";

    /**
     * The {@link KafkaTemplate} instance used for sending messages to Kafka.
     */
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;


    /**
     * <p>
     * This method sends the provided message to the Kafka topic {@link #TOPIC}.
     * </p>
     *
     * @param message the message to send to the Kafka topic
     */
    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
    }
}
