package service;

import config.ConfigLoader;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SubscriberManager {
    private static final Map<String, List<PrintWriter>> subscribers = new HashMap<>();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static int subscriberCount = 0;

    public static void subscribe(String topic, PrintWriter out) {
        if(!ConfigLoader.checkKey(topic)){
            out.println("ERROR|Rate data not found for " + topic);
        }
        else {
            if(!subscribers.containsKey(topic)){
                subscribers.computeIfAbsent(topic, k -> new ArrayList<>()).add(out);
                subscriberCount++;
                out.println("Subscribed to " + topic);
            }
            else{
                out.println("INFO|You already have a subscription for" + topic);
            }
        }
    }

    public static void unsubscribe(String topic, PrintWriter out) {
        if(!ConfigLoader.checkKey(topic)){
            out.println("ERROR|Rate data not found for " + topic);
        }
        else {
            if(subscribers.containsKey(topic)){
                subscribers.getOrDefault(topic, new ArrayList<>()).remove(out);
                out.println("Unsubscribed from " + topic);
                subscriberCount--;
            }
            else{
                out.println("INFO|You do not have subscription to " + topic);
            }
        }
    }

    public static void notifySubscribers(boolean isLargeUpdate) {
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
