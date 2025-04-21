package com.murat.mainapp.calculator;

import com.murat.mainapp.model.Rate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@link RateCalculator} Interface implementation class for performing exchange rate calculations for specific currencies.
 * <p>
 * Responsible for calculating derived rates such as USD/TRY,
 * EUR/TRY, and GBP/TRY based on provided base and cross rates.
 * </p>
 */
public class JavaRateCalculator implements RateCalculator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * Calculates the USD/TRY exchange rate using two given base rates.
     *
     * @param rate1 the first rate involved in the USD/TRY calculation
     * @param rate2 the second rate involved in the USD/TRY calculation
     * @return the calculated USD/TRY {@link Rate}
     */
    @Override
    public Rate calculateUSDTRY(Rate rate1, Rate rate2) {

        if(rate2 == null){
            rate1.setRateName("USDTRY");
            return rate1;
        }


        Rate calculatedRate = new Rate();
        double bid = (rate1.getBid() + rate2.getBid())/2.0;
        double ask = (rate1.getAsk() + rate2.getAsk())/2.0;
        String timestamp = LocalDateTime.now().format(formatter);

        calculatedRate.setBid(bid);
        calculatedRate.setAsk(ask);
        calculatedRate.setRateName("USDTRY");
        calculatedRate.setTimestamp(timestamp);

        return calculatedRate;
    }

    /**
     * Calculates the EUR/TRY exchange rate using the existing USD/TRY rate and two additional base rates.
     *
     * @param rateUSDTRY the existing USD/TRY {@link Rate}
     * @param rate1      the first rate involved in the EUR/TRY calculation
     * @param rate2      the second rate involved in the EUR/TRY calculation
     * @return the calculated EUR/TRY {@link Rate}
     */
    @Override
    public Rate calculateEURTRY(Rate rateUSDTRY , Rate rate1, Rate rate2) {
        Rate calculatedRate = new Rate();

        if( rate2 == null )
        {
            double usdMid = (rateUSDTRY.getAsk() + rateUSDTRY.getBid()) / 2.0;
            double bid = usdMid * rate1.getBid();
            double ask = usdMid * rate1.getAsk();
            String timestamp = LocalDateTime.now().format(formatter);

            calculatedRate.setBid(bid);
            calculatedRate.setAsk(ask);
            calculatedRate.setRateName("EURTRY");
            calculatedRate.setTimestamp(timestamp);

            return calculatedRate;
        }
        double usdMid = (rateUSDTRY.getAsk() + rateUSDTRY.getBid()) / 2.0;
        double bid = usdMid * ((rate1.getBid() + rate2.getBid())/2.0);
        double ask = usdMid * ((rate1.getAsk() + rate2.getAsk())/2.0);
        String timestamp = LocalDateTime.now().format(formatter);

        calculatedRate.setBid(bid);
        calculatedRate.setAsk(ask);
        calculatedRate.setRateName("EURTRY");
        calculatedRate.setTimestamp(timestamp);

        return calculatedRate;

    }

    /**
     * Calculates the GBP/TRY exchange rate using the existing GBP/TRY rate and two additional base rates.
     *
     * @param rateUSDTRY the existing USD/TRY {@link Rate}
     * @param rate1      the first rate involved in the GBP/TRY calculation
     * @param rate2      the second rate involved in the GBP/TRY calculation
     * @return the calculated GBP/TRY {@link Rate}
     */
    @Override
    public Rate calculateGBPTRY(Rate rateUSDTRY,Rate rate1, Rate rate2) {
        Rate calculatedRate = new Rate();
        if( rate2 == null )
        {
            double usdMid = (rateUSDTRY.getAsk() + rateUSDTRY.getBid()) / 2.0;
            double bid = usdMid * rate1.getBid();
            double ask = usdMid * rate1.getAsk();
            String timestamp = LocalDateTime.now().format(formatter);

            calculatedRate.setBid(bid);
            calculatedRate.setAsk(ask);
            calculatedRate.setRateName("EURTRY");
            calculatedRate.setTimestamp(timestamp);

            return calculatedRate;
        }

        double usdMid = (rateUSDTRY.getAsk() + rateUSDTRY.getBid()) / 2.0;
        double bid = usdMid * ((rate1.getBid() + rate2.getBid())/2.0);
        double ask = usdMid * ((rate1.getAsk() + rate2.getAsk())/2.0);
        String timestamp = LocalDateTime.now().format(formatter);

        calculatedRate.setBid(bid);
        calculatedRate.setAsk(ask);
        calculatedRate.setRateName("GBPTRY");
        calculatedRate.setTimestamp(timestamp);

        return calculatedRate;
    }
}
