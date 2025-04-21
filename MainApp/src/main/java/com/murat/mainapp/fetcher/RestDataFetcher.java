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

/**
 * Child of {@link PlatformDataFetcherAbstract} that communicates with a REST-FULL data provider.
 * <p>
 * This class handles authentication, periodic data fetching, subscription management, and
 * dispatching rate updates via {@link PlatformDataCallback}.
 * </p>
 *
 */
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

    /**
     * Used for holding JWT token and making a request with it
     */
    private HttpEntity<String> requestEntity;

    /**
     * A {@link Timer} which is used for sending an async request to RESTDataProducer periodically
     *
     */
    private final Timer timer = new Timer();

    /**
     * Used for making HTTP requests
     */
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Map of {@link TimerTask}s is used for holding information of subscriptions to make periodic requests.
     */
    private final Map<String, TimerTask> subscriptionTasks = new ConcurrentHashMap<>();


    /**
     * Sets the callback interface to be used for data events and connection updates.
     *
     * @param callback the callback instance implementing {@link PlatformDataCallback}
     */
    @Override
    public void setCallback(PlatformDataCallback callback) {
        this.callback = callback;
    }

    /**
     * Sets the communication port for the data platform.
     *
     * @param port the port to be used for connections
     */
    @Override
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    /**
     * Sets the base URL for the platform's API or data endpoint.
     *
     * @param baseUrl the base URL of the data source
     */
    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Connects to the RESTFULL platform and retrieves a JWT token for authentication.
     * Notifies the callback on success or failure.
     *
     * @param platformName the name of the platform
     * @param userId       the user ID (currently not used, hardcoded as 'admin')
     * @param password     the password (currently not used, hardcoded as 'admin')
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

    }

    /**
     * Disconnects from the platform by canceling all active subscriptions {@link #subscriptionTasks} and stopping the timer
     * {@link #timer}.
     *
     * @param platformName the name of the platform
     * @param userId       the user ID (optional)
     * @param password     the password (optional)
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
     * Subscribes to a specific rate feed from the platform. Data is fetched periodically
     * using a scheduled {@link #subscriptionTasks}. On first response, {@code onRateAvailable()} is called;
     * on subsequent responses, {@code onRateUpdate()} is triggered.
     *
     * @param platformName the platform name
     * @param rateName     the specific rate name
     * @throws ConnectionNotFoundException if not connected to the platform
     */
    @Override
    public void subscribe(String platformName, String rateName) {

        if (!connected) {
            logger.error("Cannot subscribe, not connected to platform: {}", platformName);
            throw new ConnectionNotFoundException("Cannot subscribe, not connected to platform: " + platformName);
        }

        // Eğer aynı rateName için zaten bir abonelik varsa, tekrar abone olmaya gerek yok
        if (subscriptionTasks.containsKey(rateName)) {
            logger.warn("Already subscribed to {} {}",platformName, rateName);
            return;
        }
        // İlk çağrı kontrolü için AtomicBoolean
        logger.info("Subscribing to {} {}",platformName, rateName);

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
     * Unsubscribes from a specific rate feed by canceling the corresponding {@link #subscriptionTasks}.
     *
     * @param platformName the platform name
     * @param rateName     the rate/feed name
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

    /**
     * Sends an authentication request to retrieve a JWT token from the REST API.
     * <p>
     * Currently uses hardcoded credentials (admin/admin) for demonstration purposes.
     *
     * @return the JWT token if authentication is successful, or {@code null} otherwise
     */
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
