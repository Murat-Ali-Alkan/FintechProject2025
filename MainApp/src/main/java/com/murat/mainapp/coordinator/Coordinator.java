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
import com.murat.mainapp.service.CurrencyService;
import com.murat.mainapp.service.KafkaProducerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

@Component
public class Coordinator implements PlatformDataCallback {

    private static final Logger logger = LogManager.getLogger(Coordinator.class);

    // Dinamik olarak yüklenecek fetcher'lar
    private List<PlatformDataFetcher> fetchers = new ArrayList<>();


    private final CacheManager cacheManager;

    //     Kafka ile haberleşme (KafkaTemplate de Spring Boot tarafından konfigüre edilmiş olmalı)
    private final KafkaProducerService kafkaProducerService;


    private final Timer timer = new Timer();

    private final Set<String> platformNames = new HashSet<>();

    private final Map<String,String> ratePlatformNames = new HashMap<>();

    private final Set<String> rateNames = new HashSet<>();

    private final CurrencyService currencyService;


    public Coordinator(CacheManager cacheManager, KafkaProducerService kafkaProducerService, CurrencyService currencyService) {
        this.cacheManager = cacheManager;
        this.kafkaProducerService = kafkaProducerService;
        this.currencyService = currencyService;
    }

    @PostConstruct
    public void init(){
        logger.info("Initializing Coordinator");
        setCalculateTimer();

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
        timer.cancel();
    }


    /**
     * Yardımcı metod: Rate nesnesini ortak formata çevirir.
     * Format: platformName|bid|ask|ISO formatlı timestamp
     */
    private String formatRate(String platformName, String rateName, Rate rate) {

        // İstenen format örneğinde sayılar farklı basamakta olabilir. Burada örnek olarak bid/ask değerlerini 2 ondalık basamakla formatladık.
        String formattedBid = String.format("%.2f", rate.getBid());
        String formattedAsk = String.format("%.2f", rate.getAsk());

        if(platformName!=null) {
            return platformName + "_"+ rateName +"|" + formattedBid + "|" + formattedAsk + "|" + rate.getTimestamp();
        }

        return rateName +"|" + formattedBid + "|" + formattedAsk + "|" + rate.getTimestamp();

    }



    public void tryCalculate() {
        Cache cache = cacheManager.getCache("raw_rates");

        if(cache ==null)
            return;

        List<Rate> rates = new ArrayList<>();

        platformNames.forEach(platformName -> {
            rateNames.forEach(name -> {
                Rate rate = cache.get(platformName + "_" +name , Rate.class);
                if (rate != null) {
                    cache.evict(platformName + "_" +name );
                    rates.add(rate);
                }
            });
        });

        if (rates.isEmpty())
            return;

        List<Rate> rateUSDTRY = rates.stream().filter(rate -> rate.getRateName().contains("USDTRY")).toList();
        List<Rate> rateEURUSD = rates.stream().filter(rate -> rate.getRateName().contains("EURUSD")).toList();
        // rateGBPUSD ?

        Rate usdTRY;
        Rate eurTRY;

        Cache calculatedCache = cacheManager.getCache("calculated_rates");
        if(rateUSDTRY.size()>1){
            usdTRY  = currencyService.calculateUSDTRY(rateUSDTRY.get(0),rateUSDTRY.get(1));

            calculatedCache.put(usdTRY.getRateName(), usdTRY);
            String formattedUSDTRY = formatRate(null,usdTRY.getRateName(),usdTRY);
            logger.info("Calculated Rate available: {}", formattedUSDTRY);
            kafkaProducerService.sendMessage(formattedUSDTRY);

            if(rateEURUSD.size()>1){
                eurTRY = currencyService.calculateEURTRY(usdTRY,rateEURUSD.get(0),rateEURUSD.get(1));
                calculatedCache.put(eurTRY.getRateName(), eurTRY);
                String formattedEURTRY = formatRate(null,eurTRY.getRateName(),eurTRY);
                logger.info("Calculated Rate available: {}", formattedEURTRY);
                kafkaProducerService.sendMessage(formattedEURTRY);
            }
            else if (rateEURUSD.size()==1){
                eurTRY = currencyService.calculateEURTRY(usdTRY,rateEURUSD.get(0),null);
                calculatedCache.put(eurTRY.getRateName(), eurTRY);
                String formattedEURTRY = formatRate(null,eurTRY.getRateName(),eurTRY);
                logger.info("Calculated Rate available: {}", formattedEURTRY);
                kafkaProducerService.sendMessage(formattedEURTRY);
            }

        }
        else if (rateUSDTRY.size() ==1){
            usdTRY = currencyService.calculateUSDTRY(rateUSDTRY.get(0),null);
            calculatedCache.put(usdTRY.getRateName(), usdTRY);
            String formattedUSDTRY = formatRate(null,usdTRY.getRateName(),usdTRY);
            logger.info("Calculated Rate available: {}", formattedUSDTRY);
            kafkaProducerService.sendMessage(formattedUSDTRY);

            if(rateEURUSD.size()>1){
                eurTRY = currencyService.calculateEURTRY(usdTRY,rateEURUSD.get(0),rateEURUSD.get(1));
                calculatedCache.put(eurTRY.getRateName(), eurTRY);
                String formattedEURTRY = formatRate(null,eurTRY.getRateName(),eurTRY);
                logger.info("Calculated Rate available: {}", formattedEURTRY);
                kafkaProducerService.sendMessage(formattedEURTRY);
            }
            else if (rateEURUSD.size()==1){
                eurTRY = currencyService.calculateEURTRY(usdTRY,rateEURUSD.get(0),null);
                calculatedCache.put(eurTRY.getRateName(), eurTRY);
                String formattedEURTRY = formatRate(null,eurTRY.getRateName(),eurTRY);
                logger.info("Calculated Rate available: {}", formattedEURTRY);
                kafkaProducerService.sendMessage(formattedEURTRY);
            }
            
        }


    }


    private void setCalculateTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                    tryCalculate();
            }
        };
        timer.scheduleAtFixedRate(task, 30000, 10000);
    }



    /**
     * @param platformName
     * @param status
     */
    @Override
    public void onConnect(String platformName, boolean status) {
        if(status){
            logger.info("Connected to platform " + platformName);
            platformNames.add(platformName);
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
        platformNames.remove(platformName);
    }

    /**
     * @param platformName
     * @param rateName
     * @param rate
     */
    @Override
    public void onRateAvailable(String platformName, String rateName, Rate rate) {

        rateNames.add(rateName);

        Cache cache = cacheManager.getCache("raw_rates");
        Cache rateCheckCache = cacheManager.getCache("rate_check");
        if(cache != null && rateCheckCache != null) {
            String key = String.format("%s_%s", platformName, rateName);
            cache.put(key, rate);
            rateCheckCache.put(key, rate);
        }
        else{
            logger.error("Rate available but no cache found for platform {}", platformName);
        }

        // Gelen veriyi ortak formata çevir
        String formattedRate = formatRate(platformName, rateName, rate);
        logger.info("Rate available: {}", formattedRate);

        // Kafka’ya gönder (örneğin "rates_topic" adlı topic’e)
        kafkaProducerService.sendMessage(formattedRate);
    }

    /**
     * @param platformName
     * @param rateName
     * @param rateFields
     */
    @Override
    public void onRateUpdate(String platformName, String rateName, RateFields rateFields) {

        Rate rate = rateFields.toRate();

        Cache cache = cacheManager.getCache("raw_rates");
        Cache rateCheckCache = cacheManager.getCache("rate_check");
        if(cache != null && rateCheckCache != null) {
            String key = String.format("%s_%s", platformName, rateName);
            Rate oldRate = rateCheckCache.get(key,Rate.class);

            if(isRateChangeAbnormal(oldRate,rate)){
                logger.info("Rate {} change abnormal for platform {} old value : {} new value : {}",rateName, platformName,oldRate.getBid(),rate.getBid());
                return;
            }

            cache.put(key, rate);
            rateCheckCache.put(key, rate);
        }
        else{
            logger.error("Rate available but no cache found for platform {}", platformName);
        }

        String formattedRate = formatRate(platformName, rateName, rate);
        logger.info("Rate update: {}", formattedRate);

        // Kafka’ya gönder
        kafkaProducerService.sendMessage(formattedRate);

    }

    /**
     * @param platformName
     * @param rateName
     * @param rateStatus
     */

    @Override
    public void onRateStatus(String platformName, String rateName, RateStatus rateStatus) {
        logger.info("Rate status for {} - {}: {}", platformName, rateName, rateStatus);

    }

    private boolean isRateChangeAbnormal(Rate oldRate, Rate newRate) {
        double oldBid = oldRate.getBid();
        double newBid = newRate.getBid();

        double difference = newBid - oldBid;

        double percentChange = difference / Math.abs(oldBid);

        return Math.abs(percentChange) > 0.01;

    }
}