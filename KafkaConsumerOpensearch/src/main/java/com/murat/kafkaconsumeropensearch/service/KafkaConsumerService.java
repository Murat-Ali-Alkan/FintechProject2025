package com.murat.kafkaconsumeropensearch.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;




@Service
public class KafkaConsumerService {
    private static final Logger kafkaLogger = LogManager.getLogger("KafkaLogger");

    @KafkaListener(topics = "test-topic", groupId = "rate-group-2")
    public void listen(String message) {
        kafkaLogger.info(message);
    }
}
