package com.murat.kafkaconsumer.service;

import com.murat.kafkaconsumer.model.Rate;
import com.murat.kafkaconsumer.repository.RateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private final RateRepository rateRepository;

    public KafkaConsumerService(RateRepository rateRepository) {
        this.rateRepository = rateRepository;
    }

    @KafkaListener(topics = "test-topic", groupId = "rate-group")
    public void listen(String message) {
        System.out.println("Alınan mesaj: " + message);
        Rate rate = stringToRate(message);
        rateRepository.save(rate);
    }

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
