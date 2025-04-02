package com.murat.restproducer.service;

import com.murat.restproducer.config.ConfigLoader;
import com.murat.restproducer.model.ExchangeRate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExchangeRateService {
    private static final Map<String, Double> exchangeRates = new HashMap<>();
    private static final Map<String,Double> exchangeLargeRates = new HashMap<>();
    private static final Random random = new Random();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final int NUMBER_OF_MAX_UPDATES = Integer.parseInt(ConfigLoader.getProperty("update.maxNumberOfTimes","15"));
    public static int USDTRYUpdates = 0;
    public static int EURUSDUpdates = 0;

    static {
        loadExchangeRates();
    }

    private static void loadExchangeRates() {
        exchangeRates.put("PF2_USDTRY", Double.parseDouble(ConfigLoader.getProperty("PF2_USDTRY", "35.02312312312")));
        exchangeRates.put("PF2_EURUSD", Double.parseDouble(ConfigLoader.getProperty("PF2_EURUSD", "1.08321321321")));
    }

    public ExchangeRate getRate(String currencyPair, boolean isLargeRate) {
        if(!exchangeRates.containsKey(currencyPair)) {
            throw new IllegalArgumentException("Invalid currency " + currencyPair);
        }
        if(isLargeRate) {
            updateLargeRates(currencyPair);
            return getLargeRate(currencyPair);
        }
        double bid = exchangeRates.getOrDefault(currencyPair, 0.0);
        double ask = 0;
        if(currencyPair.equals("PF2_USDTRY")) {
            if(USDTRYUpdates == NUMBER_OF_MAX_UPDATES) {
                throw new IllegalArgumentException("Exchange rate exceeded maximum number of updates");
            }
            ask = bid + 1;
            updateRates(currencyPair);
            USDTRYUpdates++;
        }
        else{
            if(currencyPair.equals("PF2_EURUSD")) {
                if(EURUSDUpdates == NUMBER_OF_MAX_UPDATES) {
                    throw new IllegalArgumentException("Exchange rate exceeded maximum number of updates");
                }
            }
            ask = (bid + 0.021);
            updateRates(currencyPair);
            EURUSDUpdates++;
        }
        String timestamp = LocalDateTime.now().format(formatter);
        return new ExchangeRate(currencyPair, bid, ask, timestamp);
    }


    private ExchangeRate getLargeRate(String currencyPair) {
        double bid = exchangeLargeRates.getOrDefault(currencyPair, 0.0);
        double ask = 0.0;
        if(currencyPair.equals("PF2_USDTRY")) {
            if(USDTRYUpdates == NUMBER_OF_MAX_UPDATES) {
                throw new IllegalArgumentException("Exchange rate exceeded maximum number of updates");
            }
            ask = bid + 1;
            USDTRYUpdates++;
        }
        else{
            if(currencyPair.equals("PF2_EURUSD")) {
                if(EURUSDUpdates == NUMBER_OF_MAX_UPDATES) {
                    throw new IllegalArgumentException("Exchange rate exceeded maximum number of updates");
                }
            }
            ask = bid + 0.021;
            EURUSDUpdates++;
        }
        String timestamp = LocalDateTime.now().format(formatter);
        return new ExchangeRate(currencyPair, bid, ask, timestamp);
    }

    public void updateRates(String currencyPair) {
        double currencyRate = exchangeRates.get(currencyPair);
        double changeFactor = (random.nextDouble() * 2 -1) * 0.01;
        currencyRate = currencyRate * (1+changeFactor);
        exchangeRates.put(currencyPair, currencyRate);
    }

    public void updateLargeRates(String currencyPair) {
        double currencyRate = exchangeRates.get(currencyPair);
        double changeFactor = (random.nextDouble() * 2 -1) * 0.015;
        currencyRate = currencyRate * (1+changeFactor);
        exchangeLargeRates.put(currencyPair, currencyRate);
    }


}
