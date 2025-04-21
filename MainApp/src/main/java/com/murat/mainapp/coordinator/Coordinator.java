package com.murat.mainapp.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.murat.mainapp.callback.PlatformDataCallback;
import com.murat.mainapp.config.FetcherConfig;
import com.murat.mainapp.config.FetchersConfig;
import com.murat.mainapp.exception.ConnectionNotFoundException;
import com.murat.mainapp.fetcher.PlatformDataFetcherAbstract;
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

/**
 * The {@code Coordinator} class orchestrates the dynamic loading and management of platform data fetchers,
 * processes currency rate data, calculates derived currency rates, and publishes rate updates to Kafka topics.
 * It utilizes a cache for temporary data storage and handles abnormal rate change detection.
 *
 * <p>This component is annotated with {@code @Component} for Spring dependency injection and implements
 * {@link PlatformDataCallback} to receive data-related events from the fetchers.</p>
 *
 * Responsibilities include:
 * <ul>
 *     <li>Dynamically loading fetchers from YML configuration using reflection</li>
 *     <li>Handling connection and disconnection to/from data platforms</li>
 *     <li>Storing and retrieving rate data using {@link CacheManager}</li>
 *     <li>Detecting abnormal rate changes</li>
 *     <li>Performing rate calculations using {@link CurrencyService}</li>
 *     <li>Publishing formatted rates to Kafka using {@link KafkaProducerService}</li>
 * </ul>
 */
@Component
public class Coordinator implements PlatformDataCallback {

    private static final Logger logger = LogManager.getLogger(Coordinator.class);

    // Dinamik olarak yüklenecek fetcher'lar
    private List<PlatformDataFetcherAbstract> fetchers = new ArrayList<>();

    private List<FetcherConfig> fetchersConfigs = new ArrayList<>();


    private final CacheManager cacheManager;

    //     Kafka ile haberleşme (KafkaTemplate de Spring Boot tarafından konfigüre edilmiş olmalı)
    private final KafkaProducerService kafkaProducerService;


    private final Timer timer = new Timer();

    /**
     * Used when calculating the calculated rates
     */
    private final Set<String> platformNames = new HashSet<>();

    private final Map<String,String> ratePlatformNames = new HashMap<>();

    /**
     * Used when calculating the calculated rates
     */
    private final Set<String> rateNames = new HashSet<>();

    private final CurrencyService currencyService;


    /**
     * Constructor for dependency injection.
     *
     * @param cacheManager the cache manager used for storing raw and calculated rate data
     * @param kafkaProducerService the Kafka producer service used to publish messages
     * @param currencyService the service used to perform rate calculations
     */
    public Coordinator(CacheManager cacheManager, KafkaProducerService kafkaProducerService, CurrencyService currencyService) {
        this.cacheManager = cacheManager;
        this.kafkaProducerService = kafkaProducerService;
        this.currencyService = currencyService;
    }

    /**
     * Initializes the coordinator by reading the fetcher configurations, loading the fetcher classes using reflection,
     * and establishing connections to platforms.
     */
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
                fetchersConfigs.add(fc);
                Class<?> clazz = Class.forName(fc.getClassName());
                PlatformDataFetcherAbstract fetcher = (PlatformDataFetcherAbstract) clazz.getDeclaredConstructor().newInstance();

                // Önemli: fetcher sınıfı callback için setCallback metodunu sunmalıdır
                fetcher.setCallback(this);
                fetcher.setPort(fc.getPort()); // Buna gerek olmayabilir
                fetcher.setBaseUrl(fc.getBaseUrl());
                fetcher.setPlatformName(fc.getPlatformName());
                fetcher.setUserId(fc.getUserId());
                fetcher.setPassword(fc.getPassword());

                fetchers.add(fetcher);

                //Bağlantıyı kur
                logger.info("Connecting to platform " + fc.getPlatformName());

                try {
                    fetcher.connect(fc.getPlatformName(), fc.getUserId(), fc.getPassword());
                    logger.info("Connected to platform " + fc.getPlatformName());

                    logger.info("Adding fetcher " + fc.getClassName());

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

    /**
     * Shuts down the coordinator by disconnecting all fetchers and stopping the timer.
     */
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down Coordinator");
        for (PlatformDataFetcherAbstract fetcher : fetchers) {
            fetcher.disconnect(fetcher.getPlatformName(),fetcher.getUserId(),fetcher.getPassword()); // Parametreler fetcher'a göre düzenlenebilir.
        }
        timer.cancel();
    }


    /**
     * Formats a {@link Rate} object into a standardized string.
     *
     * @param platformName the name of the platform (can be null)
     * @param rateName the name of the rate
     * @param rate the rate object to format
     * @return a string in the format platform|bid|ask|timestamp
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



    /**
     * Attempts to calculate new currency rates from the raw cached rates.
     * Sends the results to Kafka if successful.
     */
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


    /**
     * Sets a scheduled timer to repeatedly call {@link #tryCalculate()} every 10 seconds,
     * starting after a 30-second delay.
     */
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
     * Called when a platform is successfully connected.
     * Subscribes the appropriate fetcher to the specified currency pairs.
     *
     * @param platformName the name of the platform
     * @param status true if connection was successful
     */
    @Override
    public void onConnect(String platformName, boolean status) {
        if(status){
            logger.info("Connected to platform " + platformName);
            platformNames.add(platformName);
            fetchersConfigs.stream().filter(config -> config.getPlatformName().equals(platformName)).findFirst()
                    .ifPresent(config -> {
                        //Abone ol
                        try {
                            Optional<PlatformDataFetcherAbstract> fetcherOptional = fetchers.stream()
                                    .filter(f -> f.getPlatformName().equalsIgnoreCase(config.getPlatformName()))
                                    .findFirst();
                            if(fetcherOptional.isPresent()){
                                PlatformDataFetcherAbstract fetcher = fetcherOptional.get();
                                for (String currency : config.getCurrencyPairs()) {
                                    fetcher.subscribe(config.getPlatformName(), currency);
                                    logger.info("Subscribed to platform {} , currency {} " + config.getPlatformName(),currency);
                                }
                                logger.info("Registered Fetcher " + config.getClassName());
                            }
                            else{
                                logger.error("Error subscribing to platform " + config.getPlatformName());
                            }
                        }catch (Exception e){
                            logger.error("Error subscribing to platform " + config.getPlatformName());
                        }
                    });
        }
        else{
            throw new ConnectionNotFoundException("Error connecting to platform " + platformName);
        }

    }

    /**
     * Called when a platform is disconnected.
     * Removes the platform from the active list.
     *
     * @param platformName the name of the disconnected platform
     * @param status true if disconnection was successful
     */
    @Override
    public void onDisconnect(String platformName, boolean status) {
        logger.info("Disconnected from platform {} with status {}", platformName, status);
        platformNames.remove(platformName);
    }

    /**
     * Called when a new rate is received from a platform.
     * Stores the rate in cache and sends the formatted rate to Kafka.
     *
     * @param platformName the name of the platform
     * @param rateName the name of the rate
     * @param rate the received rate data
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
     * Called when a rate update is received from a platform.
     * Checks if the change is abnormal {@link #isRateChangeAbnormal(Rate, Rate)}, and if not, stores and sends the updated rate.
     *
     * @param platformName the name of the platform
     * @param rateName the name of the rate
     * @param rateFields the updated rate fields
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
     * Called when a rate's status is updated.
     * Logs the new status.
     *
     * @param platformName the platform providing the rate
     * @param rateName the name of the rate
     * @param rateStatus the new status of the rate
     */
    @Override
    public void onRateStatus(String platformName, String rateName, RateStatus rateStatus) {
        logger.info("Rate status for {} - {}: {}", platformName, rateName, rateStatus);

    }

    /**
     * Checks whether the change between two rates is abnormal by comparing bid values.
     *
     * @param oldRate the previous rate
     * @param newRate the updated rate
     * @return true if the rate change exceeds 1%, false otherwise
     */
    private boolean isRateChangeAbnormal(Rate oldRate, Rate newRate) {
        double oldBid = oldRate.getBid();
        double newBid = newRate.getBid();

        double difference = newBid - oldBid;

        double percentChange = difference / Math.abs(oldBid);

        return Math.abs(percentChange) > 0.01;

    }
}