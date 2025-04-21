package com.murat.mainapp.service;

import com.murat.mainapp.calculator.RateCalculator;
import com.murat.mainapp.exception.CalculatorNotFoundException;
import com.murat.mainapp.exception.CurrencyNotFoundException;
import com.murat.mainapp.model.Rate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * The {@code CurrencyService} class is responsible for calculating currency exchange rates
 * such as USD/TRY, EUR/TRY, and GBP/TRY using a dynamically loaded {@link RateCalculator} implementation.
 * <p>
 * This service loads the appropriate rate calculator implementation at runtime based on
 * the provided class name, enabling flexible calculation strategies.
 * </p>
 *
 */
@Service
public class CurrencyService {
    private RateCalculator rateCalculator;
    private static final Logger logger = LogManager.getLogger(CurrencyService.class);


    /**
     * Constructs a {@code CurrencyService} instance and initializes the {@link RateCalculator}
     * based on the class name provided via application properties.
     *
     * @param className the fully qualified class name of a {@link RateCalculator} implementation.
     * @throws CalculatorNotFoundException if the class cannot be loaded or instantiated.
     */
    public CurrencyService(@Value("${calculator.class}")String className) {
        try{
            Class<?> clazz = Class.forName(className);
            rateCalculator = (RateCalculator) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CalculatorNotFoundException("Couldn't load the calculator class " + className);
        }
    }

    /**
     * Calculates the USD to TRY  exchange rate using one or two provided rate values.
     *
     * @param rate1 the first rate input.
     * @param rate2 the second rate input.
     * @return the calculated USD/TRY rate.
     * @throws CurrencyNotFoundException if both {@code rate1} and {@code rate2} are {@code null}.
     */
    public Rate calculateUSDTRY(Rate rate1, Rate rate2) {
        if (rate1 == null && rate2 == null) {
            throw new CurrencyNotFoundException("Couldn't calculate rate, both rate1 and rate2 are null");
        }
        if (rate1 == null) {
            return rateCalculator.calculateUSDTRY(rate2,null);
        }

        if (rate2 == null) {
            return rateCalculator.calculateUSDTRY(rate1,null);
        }

        return rateCalculator.calculateUSDTRY(rate1,rate2);
    }

    /**
     * Calculates the EUR to TRY exchange rate based on the USD/TRY rate and additional inputs.
     *
     * @param rateUSDTRY the previously calculated USD/TRY exchange rate.
     * @param rate1 the first rate input.
     * @param rate2 the second rate input.
     * @return the calculated EUR/TRY rate.
     * @throws CurrencyNotFoundException if {@code rateUSDTRY} is {@code null} or both {@code rate1} and {@code rate2} are {@code null}.
     */
    public Rate calculateEURTRY(Rate rateUSDTRY , Rate rate1, Rate rate2) {

        if (rateUSDTRY == null)
        {
            throw new CurrencyNotFoundException("Couldn't calculate rate, both rateUSDTRY is null");
        }

        if (rate1 == null && rate2 == null) {
            throw new CurrencyNotFoundException("Couldn't calculate rate, both rate1 and rate2 are null");
        }

        if (rate1 == null) {
            return rateCalculator.calculateEURTRY(rateUSDTRY,rate2,null);
        }

        if (rate2 == null) {
            return rateCalculator.calculateEURTRY(rateUSDTRY,rate1,null);
        }

        return rateCalculator.calculateEURTRY(rateUSDTRY,rate1,rate2);
    }

    /**
     * Calculates the GBP to TRY exchange rate based on the USD/TRY rate and additional inputs.
     *
     * @param rateUSDTRY the previously calculated USD/TRY exchange rate.
     * @param rate1 the first rate input.
     * @param rate2 the second rate input.
     * @return the calculated GBP/TRY rate.
     * @throws CurrencyNotFoundException if {@code rateUSDTRY} is {@code null} or both {@code rate1} and {@code rate2} are {@code null}.
     */
    public Rate calculateGBPTRY(Rate rateUSDTRY , Rate rate1, Rate rate2) {

        if (rateUSDTRY == null)
        {
            throw new CurrencyNotFoundException("Couldn't calculate rate, both rateUSDTRY is null");
        }

        if (rate1 == null && rate2 == null) {
            throw new CurrencyNotFoundException("Couldn't calculate rate, both rate1 and rate2 are null");
        }

        if (rate1 == null) {
            return rateCalculator.calculateGBPTRY(rateUSDTRY,rate2,null);
        }

        if (rate2 == null) {
            return rateCalculator.calculateGBPTRY(rateUSDTRY,rate1,null);
        }

        return rateCalculator.calculateGBPTRY(rateUSDTRY,rate1,rate2);
    }
}
