package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;
import com.murat.mainapp.exception.ConnectionNotFoundException;
import com.murat.mainapp.model.Rate;
import com.murat.mainapp.model.RateFields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RestDataFetcher extends PlatformDataFetcherAbstract{

    private static final Logger logger = LogManager.getLogger(RestDataFetcher.class);
    private int port;
    private String baseUrl;
    private PlatformDataCallback callback;
    private String platformName;
    private String userId;
    private String password;
    private boolean connected = false;
    private HttpEntity<String> requestEntity;

    private final Timer timer = new Timer();
    private final RestTemplate restTemplate = new RestTemplate();

    // Her rateName için TimerTask referanslarını tutan map
    private final Map<String, TimerTask> subscriptionTasks = new ConcurrentHashMap<>();


    /**
     * @param callback
     */
    @Override
    public void setCallback(PlatformDataCallback callback) {
        this.callback = callback;
    }

    /**
     * @param port
     */
    @Override
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    /**
     * @param baseUrl
     */
    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @param platformName
     * @param userId
     * @param password
     */
    @Override
    public void connect(String platformName, String userId, String password) {

        String token = sendTokenRequest();

        if(token != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization","Bearer " +token );
            requestEntity = new HttpEntity<>(headers);
            connected = true;
            callback.onConnect(platformName,true);
        }
        else{
            connected = false;
            callback.onConnect(platformName,false);
        }



        connected = true;
        callback.onConnect(platformName,true);
//        log.info("Connected to platform: {}", platformName);
    }

    /**
     * @param platformName
     * @param userId
     * @param password
     */
    @Override
    public void disconnect(String platformName, String userId, String password) {
        if (!connected) {
            logger.warn("Already disconnected from {}", platformName);
            return;
        }

        // Tum abonelikleri iptal et
        subscriptionTasks.forEach((rateName, task) -> {
            task.cancel();
            logger.info("Unsubscribed from {}", rateName);
        });

        subscriptionTasks.clear();
        timer.cancel();
        connected = false;
        callback.onDisconnect(platformName,true);
        logger.info("Disconnected from {}", platformName);
    }

    /**
     * @param platformName
     * @param rateName
     */
    @Override
    public void subscribe(String platformName, String rateName) {

        if (!connected) {
            logger.error("Cannot subscribe, not connected to platform: {}", platformName);
            throw new ConnectionNotFoundException("Cannot subscribe, not connected to platform: " + platformName);
        }

        // Eğer aynı rateName için zaten bir abonelik varsa, tekrar abone olmaya gerek yok
        if (subscriptionTasks.containsKey(rateName)) {
            logger.warn("Already subscribed to {}", rateName);
            return;
        }
        // İlk çağrı kontrolü için AtomicBoolean
        logger.info("Subscribing to {}", rateName);

        final AtomicBoolean firstCall = new AtomicBoolean(true);

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    String url = baseUrl + "api/rates/" + platformName + "_" + rateName;
                    try {
                        Rate rate = restTemplate.exchange(
                                url,
                                HttpMethod.GET,
                                        requestEntity,
                                Rate.class)
                                .getBody();

                        if (rate != null) {
                            if (firstCall.getAndSet(false)) {
                                // İlk veri geldiğinde
                                callback.onRateAvailable(platformName, rateName, rate);
                            } else {
                                // Sonraki verilerde
                                // Burada Rate'in RateFields ile uyumlu olduğunu varsayıyoruz.
                                RateFields rateFields = rate.toRateFields();
                                callback.onRateUpdate(platformName, rateName, rateFields);
                            }
                        } else {
                            logger.warn("No data received for {}. Unsubscribing...", rateName);
                            unsubscribe(platformName, rateName);
                        }
                    } catch (HttpClientErrorException ex) {
                        if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                            logger.warn("Rate {} not found. Unsubscribing...", rateName);
                            unsubscribe(platformName, rateName);
                        } else {
                            logger.error("Error fetching data for {}: {}", rateName, ex.getMessage());
                        }
                    }
                }catch (ResourceAccessException ex){
                    logger.error("Error fetching data for {}: {}", rateName, ex.getMessage());
                }
            }
        };

        // TimerTask'i hem planla hem de map'e ekle
        subscriptionTasks.put(rateName, task);
        timer.scheduleAtFixedRate(task, 25000, 30000);

    }

    /**
     * @param platformName
     * @param rateName
     */
    @Override
    public void unsubscribe(String platformName, String rateName) {
        TimerTask task = subscriptionTasks.get(rateName);

        if (task != null) {
            task.cancel();
            subscriptionTasks.remove(rateName);
            logger.info("Unsubscribed from {}", rateName);
        } else {
            logger.warn("No active subscription found for {}", rateName);
        }
    }

    public String sendTokenRequest() {
        String url = this.baseUrl + "token";

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin", "admin");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        String token = responseEntity.getBody();

        System.out.println(token);
        return token;
    }
}
