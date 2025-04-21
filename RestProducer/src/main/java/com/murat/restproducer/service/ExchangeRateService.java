package com.murat.restproducer.service;

import com.murat.restproducer.config.ConfigLoader;
import com.murat.restproducer.model.ExchangeRate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Service class responsible for managing and generating exchange rate data.
 *
 * <p>This class simulates real-time exchange rate updates for predefined
 * currency pairs such as <b>USDTRY</b> and <b>EURUSD</b>. It can also generate
 * higher variation ("large"/"abnormal") rates upon request.</p>
 *
 * <p>Configuration values such as maximum update counts and initial rates are
 * read from an external properties file via {@link ConfigLoader}.</p>
 *
 * <p>Each request to {@link #getRate(String, boolean)} updates
 * the rates unless the maximum update count is reached.</p>
 *
 * @see ExchangeRate
 * @see ConfigLoader
 */
public class ExchangeRateService {

    /** For storing latest normal rate
     *  <p>Holds rateName-value pair</p>
     */
    private static final Map<String, Double> exchangeRates = new HashMap<>();

    /** For storing latest abnormal rate
     *  <p>Holds rateName-value pair</p>
     */

    private static final Map<String,Double> exchangeLargeRates = new HashMap<>();

    // For generating random value
    private static final Random random = new Random();

    // DateTime Formatter
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Maximum number of update times which is read from {@code "config.properties"} file
     * with the help of {@link ConfigLoader}
     */
    private static final int NUMBER_OF_MAX_UPDATES = Integer.parseInt(ConfigLoader.getProperty("update.maxNumberOfTimes","15"));

    /**
     * Keeps the number of updates for {@code "USDTRY"} rate
     */
    public static int USDTRYUpdates = 0;

    /**
     * Keeps the number of updates for {@code "EURUSD"} rate
     */
    public static int EURUSDUpdates = 0;

    // static constructor
    static {
        loadExchangeRates();
    }

    /**
     * Loads the first exchange rates from {@code "config.properties} file
     * with the help of {@link ConfigLoader} and stores them in {@code "exchangeRates"} map
     */
    private static void loadExchangeRates() {
        exchangeRates.put("PF2_USDTRY", Double.parseDouble(ConfigLoader.getProperty("PF2_USDTRY", "35.02312312312")));
        exchangeRates.put("PF2_EURUSD", Double.parseDouble(ConfigLoader.getProperty("PF2_EURUSD", "1.08321321321")));
    }

    /**
     *
     * This method is used by {@link com.murat.restproducer.controller.ExchangeRateController} controller to simulate real-time exchange data
     *
     * @param currencyPair is the rateName of the {@link ExchangeRate} which will be sent. (ex."PF2_USDTRY")
     *                     Throws {@link IllegalArgumentException} if the rateName is not defined
     *                     or the rate update count has exceeded number of max updates  .
     * <br>
     * @param isLargeRate is a variable of type boolean
     *                    that checks whether the update should be "large"/"abnormal" or
     *                    "normal"
     * @return {@link ExchangeRate} of a related currencyPair. Could be abnormal or normal depending on {@code isLargeRate} variable.
     * If {@code isLargeRate} is true function calls {@link #getLargeRate(String)} method and returns the result
     */
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


    /**
     * This method is used by {@code getRate} method to simulate real-time "abnormal" exchange data
     * @param currencyPair is the rateName of the {@link ExchangeRate} which will be sent. (ex."PF2_USDTRY")
     *                     Throws {@link IllegalArgumentException} if the rate update count has exceeded number of max updates  .
     * @return abnormal {@link ExchangeRate} of a related currencyPair.
     */
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

    /**
     *  This method updates rates in a "normal" way.
     *  It also updates {@link #exchangeRates} map to store the latest rate values
     *
     * @param currencyPair is the rateName of the {@link ExchangeRate} which will be sent. (ex."PF2_USDTRY")
     */
    public void updateRates(String currencyPair) {
        double currencyRate = exchangeRates.get(currencyPair);
        double changeFactor = (random.nextDouble() * 2 -1) * 0.01;
        currencyRate = currencyRate * (1+changeFactor);
        exchangeRates.put(currencyPair, currencyRate);
    }

    /**
     *  This method updates rates in an "abnormal" way.
     *  It also updates {@link #exchangeLargeRates} map to store the latest rate values
     *
     * @param currencyPair is the rateName of the {@link ExchangeRate} which will be sent. (ex."PF2_USDTRY")
     */
    public void updateLargeRates(String currencyPair) {
        double currencyRate = exchangeRates.get(currencyPair);
        double changeFactor = (random.nextDouble() * 2 -1) * 0.015;
        currencyRate = currencyRate * (1+changeFactor);
        exchangeLargeRates.put(currencyPair, currencyRate);
    }


}
