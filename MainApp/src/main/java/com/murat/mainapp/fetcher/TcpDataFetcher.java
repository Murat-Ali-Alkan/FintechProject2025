package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;
import com.murat.mainapp.exception.ConnectionNotFoundException;
import com.murat.mainapp.model.Rate;
import com.murat.mainapp.model.RateFields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The {@code TcpDataFetcher} class is responsible for fetching data from a platform over TCP.
 * It extends {@link PlatformDataFetcherAbstract} and implements functionalities such as connecting
 * to the platform, reading data, subscribing to rates, and unsubscribing.
 *
 * <p>Note: This class uses a {@link Timer} and {@link TimerTask} for scheduling subscriptions.</p>
 *
 * @see Timer
 * @see TimerTask
 */
@Component
public class TcpDataFetcher extends PlatformDataFetcherAbstract {

    private static final Logger logger = LogManager.getLogger(TcpDataFetcher.class);
    private static int responseCount = 0;

    private int port;
    private String baseUrl;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private PlatformDataCallback callback;
    private String platformName;
    private String userId;
    private String password;
    private boolean connected = false;

    private final Timer timer = new Timer();
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Map of {@link TimerTask}s is used for holding information of subscriptions.
     */
    private final Map<String, TimerTask> subscriptionTasks = new ConcurrentHashMap<>();

    // Abone olunan rate'leri işaretlemek için
    private final Map<String, Boolean> subscribedRates = new ConcurrentHashMap<>();

    // Merkezi okuma thread'inin çalışıp çalışmadığını kontrol etmek için
    private final AtomicBoolean readerThreadStarted = new AtomicBoolean(false);



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
     * Connects to the platform using the provided credentials and starts a background thread {@link #startReaderThread()}
     * to listen for data.
     *
     * @param platformName the name of the platform
     * @param userId the user ID
     * @param password the password
     */
    @Override
    public void connect(String platformName, String userId, String password) {
        this.platformName = platformName;
        this.userId = userId;
        this.password = password;

        try {
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gönderilen kullanıcı bilgileri
            out.println(userId);
            out.println(password);

            String response;
            // Sunucudan "OK" gelene kadar bekle
            while ((response = in.readLine()) != null) {
                if (response.equalsIgnoreCase("OK")) {
                    connected = true;
                    break;
                }
            }
            callback.onConnect(platformName, connected);

            // Bağlantı sağlandıktan sonra merkezi okuma thread'ini başlat
            startReaderThread();
        } catch (Exception e) {
            callback.onConnect(platformName, false);
        }
    }

    /**
     * Starts a thread that continuously reads data from the TCP stream.
     */
    private void startReaderThread() {
        if (readerThreadStarted.compareAndSet(false, true)) {
            new Thread(() -> {
                try {
                    String response;
                    while (connected && (response = in.readLine()) != null) {
                        String[] tokens = response.split("\\|");
                        if (tokens.length < 4) {
                            logger.info(response);
                            continue;
                        }
                        Rate rate = parseRateFromMessage(tokens);
                        if(responseCount < 2){
                            callback.onRateAvailable(platformName,rate.getRateName(),rate);
                            responseCount++;
                        }else{
                        callback.onRateUpdate(platformName, rate.getRateName(), rate.toRateFields());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error in reader thread: {}", e.getMessage());
                }
            }).start();
        }
    }



    /**
     * Disconnects from the platform and cancels all active subscriptions.
     *
     * @param platformName the name of the platform
     * @param userId the user ID
     * @param password the password
     */
    @Override
    public void disconnect(String platformName, String userId, String password) {
        if (!connected) {
            logger.warn("Already disconnected from {}", platformName);
            return;
        }

        subscriptionTasks.forEach((rateName, task) -> {
            unsubscribe(platformName, rateName);
            task.cancel();
        });

        subscriptionTasks.clear();
        subscribedRates.clear();
        timer.cancel();
        connected = false;
        out.println("exit");
        logger.info("Disconnected from {}", platformName);
    }


    /**
     * Subscribes to a specific rate on the platform.
     *
     * @param platformName the platform name
     * @param rateName the rate name to subscribe to
     */
    @Override
    public void subscribe(String platformName, String rateName) {
        if (!connected) {
            logger.error("Cannot subscribe, not connected to platform: {}", platformName);
            throw new ConnectionNotFoundException("Cannot subscribe, not connected to platform: " + platformName);
        }

        if (subscriptionTasks.containsKey(rateName)) {
            logger.warn("Already subscribed to {}", rateName);
            return;
        }

        logger.info("Subscribing to {}", rateName);

        // Subscribe komutunu yalnızca 1 kere göndermek için
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    out.println("subscribe|" + platformName + "_" + rateName);
                    logger.info("Sent subscribe command for rate: {}", rateName);
                    subscribedRates.put(rateName, true);
                } catch (Exception e) {
                    logger.error("Error during subscribe: {}", e.getMessage());
                }
                // Gönderim tamamlandıktan sonra task iptal edilir.
                this.cancel();
            }
        };

        subscriptionTasks.put(rateName, task);
        timer.schedule(task, 1);  // 5 saniye gecikmeyle gönderilir.
    }


    /**
     * Parses a received message and converts it into a {@link Rate} object.
     *
     * @param tokens the message split into tokens
     * @return the parsed {@link Rate} object
     */
    private Rate parseRateFromMessage(String[] tokens) {
        try {
            // Token 0: "PF1_EURUSD"
            String[] nameParts = tokens[0].split("_");
            // Platform kısmı tokens[0]'ın ilk kısmı; rateName tokens[0]'ın ikinci kısmı
            String incomingPlatform = nameParts[0];
            String rateName = nameParts.length > 1 ? nameParts[1] : "";
            // Eğer gelen platform ismi beklenenden farklı ise loglama yapabiliriz
            if (!incomingPlatform.equals(platformName)) {
                logger.warn("Incoming platform {} does not match expected platform {}", incomingPlatform, platformName);
            }

            // Token 1: "22:number:<bid>"
            String[] bidParts = tokens[1].split(":");
            double bid = bidParts.length >= 3 ? Double.parseDouble(bidParts[2]) : 0.0;

            // Token 2: "25:number:<ask>"
            String[] askParts = tokens[2].split(":");
            double ask = askParts.length >= 3 ? Double.parseDouble(askParts[2]) : 0.0;

            // Token 3: "5:timestamp:<timestamp>"
            String[] tsParts = tokens[3].split(":");
            String timestamp = tsParts.length >= 4 ? tsParts[2] +":" + tsParts[3]+":" + tsParts[4]  : "";

            return new Rate(rateName, bid, ask, timestamp);
        } catch (Exception e) {
            logger.error("Error parsing rate message: {}", e.getMessage());
            return new Rate();
        }
    }


    /**
     * Unsubscribes from a specific rate on the platform.
     *
     * @param platformName Name of the platform.
     * @param rateName     Name of the rate to unsubscribe from.
     */
    @Override
    public void unsubscribe(String platformName, String rateName) {
        if (!connected) {
            logger.error("Cannot unsubscribe, not connected to platform: {}", platformName);
            throw new ConnectionNotFoundException("Cannot unsubscribe, not connected to platform: " + platformName);
        }

        if (!subscribedRates.containsKey(rateName)) {
            logger.warn("Not subscribed to {}", rateName);
            return;
        }

        logger.info("Unsubscribing from platform {} rate {} ",platformName, rateName);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Future<?> future = executor.schedule(() -> {
            try {
                out.println("unsubscribe|" + platformName + "_" + rateName);
                logger.info("Sent unsubscribe command for rate: {}", rateName);
                subscribedRates.remove(rateName);
                logger.info("Unsubscribed from platform {} rate {} ", platformName, rateName);
            } catch (Exception e) {
                logger.error("Error during unsubscribe: {}", e.getMessage());
            }
        }, 1000, TimeUnit.MILLISECONDS);

        try {
            future.get();
        } catch (Exception e) {
            logger.error("Couldn't unsubscribe from {}_{}", platformName, rateName);
        } finally {
            executor.shutdown();
        }
    }
}

