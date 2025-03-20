package com.murat.mainapp.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.murat.mainapp.callback.PlatformDataCallback;
import com.murat.mainapp.config.FetcherConfig;
import com.murat.mainapp.config.FetchersConfig;
import com.murat.mainapp.exception.ConnectionNotFoundException;
import com.murat.mainapp.fetcher.PlatformDataFetcher;
import com.murat.mainapp.model.Rate;
import com.murat.mainapp.model.RateFields;
import com.murat.mainapp.model.RateStatus;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Component
public class Coordinator implements PlatformDataCallback {

    private static final Logger logger = LogManager.getLogger(Coordinator.class);

    // Dinamik olarak yüklenecek fetcher'lar
    private List<PlatformDataFetcher> fetchers = new ArrayList<>();

    // Redis cache (RedisTemplate konfigürasyonu application.yml’de veya başka bir konfigürasyon sınıfında yapılmalıdır)
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;

    // Kafka ile haberleşme (KafkaTemplate de Spring Boot tarafından konfigüre edilmiş olmalı)
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;


    @PostConstruct
    public void init(){
        logger.info("Initializing Coordinator");

        try{
            // fetchers.yml dosyasını classpath'ten oku
            InputStream in = getClass().getResourceAsStream("/fetchers.yml");
            if (in == null) {
                logger.error("Couldn't find fetchers");
            } else {
                logger.info("Fetchers loaded");
            }

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            FetchersConfig config = mapper.readValue(in, FetchersConfig.class);

            // Her bir fetcher icin

            for(FetcherConfig fc : config.getFetchers()){
                // Belirtilen sinifi reflection ile yukle
                Class<?> clazz = Class.forName(fc.getClassName());
                PlatformDataFetcher fetcher = (PlatformDataFetcher) clazz.getDeclaredConstructor().newInstance();

                // Önemli: fetcher sınıfı callback için setCallback metodunu sunmalıdır
                fetcher.setCallback(this);
                fetcher.setPort(fc.getPort()); // Buna gerek olmayabilir
                fetcher.setBaseUrl(fc.getBaseUrl());


                //Bağlantıyı kur
                logger.info("Connecting to platform " + fc.getPlatformName());

                try {
                    fetcher.connect(fc.getPlatformName(), fc.getUserId(), fc.getPassword());
                    logger.info("Connected to platform " + fc.getPlatformName());

                    //Abone ol
                    try {
                        for (String currency : fc.getCurrencyPairs()) {
                            fetcher.subscribe(fc.getPlatformName(), currency);
                            logger.info("Subscribed to platform " + fc.getPlatformName());
                        }
                        logger.info("Adding fetcher " + fc.getClassName());
                        fetchers.add(fetcher);

                        logger.info("Registered Fetcher " + fc.getClassName());

                    }catch (Exception e){
                        logger.error("Error subscribing to platform " + fc.getPlatformName());
                    }

                }
                catch(ConnectionNotFoundException e){
                    logger.error(e.getMessage());
                }
                catch(Exception e){
                    logger.error("Something went wrong while connecting to platform " + fc.getPlatformName());
                }

            }
        }catch (Exception e){
            logger.error("Error while initializing Fetcher");
        }
    }

    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Coordinator");
        for (PlatformDataFetcher fetcher : fetchers) {
            fetcher.disconnect("dummy", "dummy", "dummy"); // Parametreler fetcher'a göre düzenlenebilir.
        }
    }


    /**
     * Yardımcı metod: Rate nesnesini ortak formata çevirir.
     * Format: platformName|bid|ask|ISO formatlı timestamp
     */
    private String formatRate(String platformName, String rateName, Rate rate) {

        // İstenen format örneğinde sayılar farklı basamakta olabilir. Burada örnek olarak bid/ask değerlerini 2 ondalık basamakla formatladık.
        String formattedBid = String.format("%.2f", rate.getBid());
        String formattedAsk = String.format("%.2f", rate.getAsk());

        return platformName + "_"+ rateName +"|" + formattedBid + "|" + formattedAsk + "|" + rate.getTimestamp();
    }



    /**
     * @param platformName
     * @param status
     */
    @Override
    public void onConnect(String platformName, boolean status) {
        if(status){
            logger.info("Connected to platform " + platformName);
        }
        else{
            throw new ConnectionNotFoundException("Error connecting to platform " + platformName);
        }

    }

    /**
     * @param platformName
     * @param status
     */
    @Override
    public void onDisconnect(String platformName, boolean status) {
        logger.info("Disconnected from platform {} with status {}", platformName, status);
    }

    /**
     * @param platformName
     * @param rateName
     * @param rate
     */
    @Override
    public void onRateAvailable(String platformName, String rateName, Rate rate) {
        logger.info("Rate available for platform {} - Rate {} = {} ", platformName, rateName, rate);

    }

    /**
     * @param platformName
     * @param rateName
     * @param rateFields
     */
    @Override
    public void onRateUpdate(String platformName, String rateName, RateFields rateFields) {
        logger.info("Update rate for platform {} - RateFields {} = {} ", platformName,rateName, rateFields.toString());
    }

    /**
     * @param platformName
     * @param rateName
     * @param rateStatus
     */

    @Override
    public void onRateStatus(String platformName, String rateName, RateStatus rateStatus) {

    }
}