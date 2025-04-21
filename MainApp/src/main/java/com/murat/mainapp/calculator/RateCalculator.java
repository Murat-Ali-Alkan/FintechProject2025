package com.murat.mainapp.calculator;

import com.murat.mainapp.model.Rate;


/**
 * Interface for performing exchange rate calculations for specific currencies.
 * <p>
 * Implementations of this interface are responsible for calculating derived rates such as USD/TRY,
 * EUR/TRY, and GBP/TRY based on provided base and cross rates.
 * </p>
 */
public interface RateCalculator {

    /**
     * Calculates the USD/TRY exchange rate using two given base rates.
     *
     * @param rate1 the first rate involved in the USD/TRY calculation
     * @param rate2 the second rate involved in the USD/TRY calculation
     * @return the calculated USD/TRY {@link Rate}
     */
    Rate calculateUSDTRY(Rate rate1, Rate rate2);

    /**
     * Calculates the EUR/TRY exchange rate using the existing USD/TRY rate and two additional base rates.
     *
     * @param rateUSDTRY the existing USD/TRY {@link Rate}
     * @param rate1      the first rate involved in the EUR/TRY calculation
     * @param rate2      the second rate involved in the EUR/TRY calculation
     * @return the calculated EUR/TRY {@link Rate}
     */
    Rate calculateEURTRY(Rate rateUSDTRY , Rate rate1, Rate rate2);

    /**
     * Calculates the GBP/TRY exchange rate using the existing GBP/TRY rate and two additional base rates.
     *
     * @param rateUSDTRY the existing USD/TRY {@link Rate}
     * @param rate1      the first rate involved in the GBP/TRY calculation
     * @param rate2      the second rate involved in the GBP/TRY calculation
     * @return the calculated GBP/TRY {@link Rate}
     */
    Rate calculateGBPTRY(Rate rateUSDTRY, Rate rate1, Rate rate2);
}
