package service;

import config.ConfigLoader;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Manages subscriber registrations and notifications for currency rate topics.
 *
 * <p>This class maintains a mapping of currency rate topics to their subscribers (via {@link PrintWriter}),
 * and provides functionality to subscribe, unsubscribe, and send notifications based on updates
 * from {@link ExchangeRateManager}.</p>
 *
 * <p>Each notification message includes the topic, current bid and ask prices, and a timestamp.
 * The system supports two update types: normal and large/abnormal updates.</p>
 *
 * @see ExchangeRateManager
 * @see ConfigLoader
 */
public class SubscriberManager {

    /**
     * Subscribers with rateName - PrintWriter pair
     */
    private static final Map<String, List<PrintWriter>> subscribers = new HashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    /**
     * Total number of active subscribers.
     * */
    public static int subscriberCount = 0;

    /**
     * Subscribes a client to a specific currency topic.
     *
     * @param topic the currency rateName to subscribe to
     * @param out the output stream to send messages to the client
     */
    public static void subscribe(String topic, PrintWriter out) {
        if(!ConfigLoader.checkKey(topic)){
            out.println("ERROR|Rate data not found for " + topic);
        }
        else {
            if(!subscribers.containsKey(topic)){
                subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(out);
                subscriberCount++;
                out.println("Subscribed to " + topic);
                System.out.println("subscribed to " + topic);
            }
            else{
                out.println("INFO|You already have a subscription for " + topic);
            }
        }
    }

    /**
     * Unsubscribes a client from a specific currency topic.
     *
     * @param topic the currency rate topic to unsubscribe from
     * @param out the output stream associated with the client
     */
    public static void unsubscribe(String topic, PrintWriter out) {
        if(!ConfigLoader.checkKey(topic)){
            out.println("ERROR|Rate data not found for " + topic);
        }
        else {
            if(subscribers.containsKey(topic)){
                subscribers.remove(topic);
                subscribers.getOrDefault(topic, new ArrayList<>()).remove(out);
                out.println("Unsubscribed from " + topic);
                System.out.println("Unsubscribed from " + topic);
                subscriberCount--;
            }
            else{
                out.println("INFO|You do not have subscription to " + topic);
            }
        }
    }

    /**
     * Notifies subscribed client with the latest rate for their topic.
     *
     * <p>This method fetches either a normal or large/abnormal update from
     * {@link ExchangeRateManager}, constructs a message, and sends it to subscriber.</p>
     *
     * @param isLargeUpdate flag indicating whether the update is large
     */
    public static void
    notifySubscribers(boolean isLargeUpdate) {
        for (Map.Entry<String, List<PrintWriter>> entry : subscribers.entrySet()) {
            String topic = entry.getKey();
            double rate = 0.0;
            if(isLargeUpdate){
                rate = ExchangeRateManager.getLargeRate(topic);
            }
            else{
                rate = ExchangeRateManager.getRate(topic);
            }
            String message = "";
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            if(topic.startsWith("PF1_EURUSD")) {
                message = topic + "|22:number:" + rate + "|25:number:" + (rate + 0.21)
                        + "|5:timestamp:"+ timestamp;
            }
            else{
                message = topic + "|22:number:" + rate + "|25:number:" + (rate + 1)
                        + "|5:timestamp:"+ timestamp;
            }
            for (PrintWriter out : entry.getValue()) {
                out.println(message);
            }
        }
    }
}
