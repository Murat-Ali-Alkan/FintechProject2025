package utilitity;

import config.ConfigLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExchangeRateManager {
    private static final Map<String, Double> exchangeRates = new HashMap<>();
    private static final Map<String, Double> exchangeLargeRates = new HashMap<>();
    private static final Random random = new Random();

    static {
        loadExchangeRates();
    }

    private static void loadExchangeRates()  {
        exchangeRates.put("PF1_USDTRY", Double.parseDouble(ConfigLoader.getProperty("PF1_USDTRY", "35.02312312312")));
        exchangeRates.put("PF1_EURUSD", Double.parseDouble(ConfigLoader.getProperty("PF1_EURUSD", "1.08321321321")));
    }

    public static double getRate(String currencyPair) {
        return exchangeRates.getOrDefault(currencyPair, 0.0);
    }

    public static double getLargeRate(String currencyPair) {
        return exchangeLargeRates.getOrDefault(currencyPair, 0.0);
    }


    public static void updateRates() {
        exchangeRates.forEach((key, value) -> {
            double changeFactor = (random.nextDouble() * 2 -1) * 0.01;
            value = value * (1 + changeFactor);
            exchangeRates.put(key, value);
        });
    }

    public static void updateLargeRates() {
        exchangeRates.forEach((key, value) -> {
            double changeFactor = (random.nextDouble() * 2 -1) * 0.015;
            value = value * (1 + changeFactor);
            exchangeLargeRates.put(key, value);
        });
    }
}