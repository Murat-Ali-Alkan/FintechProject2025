package com.murat.kafkaconsumer.service;

import com.murat.kafkaconsumer.model.Rate;
import com.murat.kafkaconsumer.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


/**
 * Service class responsible for consuming messages from Kafka and
 * persisting them to the database as {@link Rate} entities.
 *
 * <p>This class listens to Kafka messages from the topic {@code "test-topic"}
 * using the consumer group {@code "rate-group"}. When a message is received,
 * it is parsed and transformed into a {@code Rate} object which is then saved
 * to the database via the {@link RateRepository}.</p>
 *
 */
@Service
public class KafkaConsumerService {

    private final RateRepository rateRepository;

    /**
     * Constructs a new {@code KafkaConsumerService} with the given repository.
     *
     * @param rateRepository the repository used to persist {@link Rate} entities
     */
    public KafkaConsumerService(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }


    /**
     * Kafka listener method that is triggered when a new message is received
     * on the topic {@code "test-topic"}.
     *
     * <p>The incoming message is expected to be in a delimited string format (e.g., {@code "PF2_USDTRY|34.80|35.10|2024-12-16T16:07:16.504"}).
     * It is parsed and mapped to a {@link Rate} object which is then persisted.</p>
     *
     * @param message the raw string message received from Kafka
     */
    @KafkaListener(topics = "test-topic", groupId = "rate-group")
    public void listen(String message) {
        System.out.println("Alınan mesaj: " + message);
        Rate rate = stringToRate(message);
        rateRepository.save(rate);
    }

    /**
     * Converts a Kafka message string into a {@link Rate} entity.
     *
     * <p>The expected input format is a pipe-separated string:
     * {@code "PF2_USDTRY|34.80|35.10|2024-12-16T16:07:16.504"}</p>
     *
     * <p>Prefix {@code PF1_} or {@code PF2_} will be stripped from the rate name,
     * and bid/ask values will be parsed as {@code double}.</p>
     *
     * @param formattedRate the string message received from Kafka
     * @return a populated {@link Rate} object ready to be persisted
     * @see Rate
     */
    public Rate stringToRate(String formattedRate)
    {
        // "PF2_USDTRY|34.80|35.10|2024-12-16T16:07:16.504" gibi bir input bekleniyor
        String[] parts = formattedRate.split("\\|");

        // rateName alanı üzerinde "PF2_" kontrolü
        String rateName = parts[0];
        if (rateName.startsWith("PF2_") || rateName.startsWith("PF1_")) {
            rateName = rateName.substring(4);
        }

        // Bid ve Ask değerlerinin double'a çevrilmesi
        double bid = Double.parseDouble(parts[1]);
        double ask = Double.parseDouble(parts[2]);

        // Zaman bilgisini direkt String olarak alıyoruz, gerekirse LocalDateTime olarak da işlenebilir
        String timestamp = parts[3];

        // Rate nesnesini oluşturup ilgili alanlara atama yapıyoruz
        Rate rate = new Rate();
        rate.setRateName(rateName);
        rate.setBid(bid);
        rate.setAsk(ask);
        rate.setTimestamp(timestamp);

        return rate;

    }
}
