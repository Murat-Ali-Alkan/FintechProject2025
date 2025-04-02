package com.murat.mainapp.calculator;

import com.murat.mainapp.model.Rate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JavaRateCalculator implements RateCalculator {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    /**
     * @param rate1
     * @param rate2
     * @return
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
     * @param rate1
     * @param rate2
     * @return
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
     * @param rate1
     * @param rate2
     * @return
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
