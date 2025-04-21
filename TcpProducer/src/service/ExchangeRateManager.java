package service;

import config.ConfigLoader;

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
 * <p>Each request to {@link #getRate(String)} or {@link #getLargeRate(String)} updates
 * the rates unless the maximum update count is reached.</p>
 * @see ConfigLoader
 */
public class ExchangeRateManager {

    /** For storing latest normal rate
     *  <p>Holds rateName-value pair</p>
     */
    private static final Map<String, Double> exchangeRates = new HashMap<>();

    /** For storing latest abnormal rate
     *  <p>Holds rateName-value pair</p>
     */
    private static final Map<String, Double> exchangeLargeRates = new HashMap<>();

    // For generating random value
    private static final Random random = new Random();

    // static constructor
    static {
        loadExchangeRates();
    }

    /**
     * Loads the first exchange rates from {@code "config.properties} file
     * with the help of {@link ConfigLoader} and stores them in {@code "exchangeRates"} map
     */
    private static void loadExchangeRates()  {
        exchangeRates.put("PF1_USDTRY", Double.parseDouble(ConfigLoader.getProperty("PF1_USDTRY", "35.02312312312")));
        exchangeRates.put("PF1_EURUSD", Double.parseDouble(ConfigLoader.getProperty("PF1_EURUSD", "1.08321321321")));
    }

    /**
     * This method gets the latest currency value from {@link #exchangeRates} for a given rateName (ex."PF1_USDTRY")
     * @param currencyPair is the rateName of the currency which will be sent. (ex."PF2_USDTRY")
     * @return latest currency value (ex. "33.22") of a given currecyPair.
     */
    public static double getRate(String currencyPair) {
        return exchangeRates.getOrDefault(currencyPair, 0.0);
    }

    /**
     * This method gets the latest currency value from {@link #exchangeLargeRates} for a given rateName (ex."PF1_USDTRY")
     * @param currencyPair is the rateName of the currency which will be sent. (ex."PF2_USDTRY")
     * @return latest currency value (ex. "33.22") of a given currecyPair.
     */
    public static double getLargeRate(String currencyPair) {
        return exchangeLargeRates.getOrDefault(currencyPair, 0.0);
    }


    /**
     *  This method updates rates in a "normal" way.
     *  It also updates {@link #exchangeRates} map to store the latest rate values
     *
     */
    public static void updateRates() {
        exchangeRates.forEach((key, value) -> {
            double changeFactor = (random.nextDouble() * 2 -1) * 0.01;
            value = value * (1 + changeFactor);
            exchangeRates.put(key, value);
        });
    }

    /**
     *  This method updates rates in an "abnormal" way.
     *  It also updates {@link #exchangeLargeRates} map to store the latest rate values
     *
     */
    public static void updateLargeRates() {
        exchangeRates.forEach((key, value) -> {
            double changeFactor = (random.nextDouble() * 2 -1) * 0.02;
            value = value * (1 + changeFactor);
            exchangeLargeRates.put(key, value);
        });
    }
}