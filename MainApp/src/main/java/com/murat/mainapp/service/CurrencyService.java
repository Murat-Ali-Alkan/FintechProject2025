package com.murat.mainapp.service;

import com.murat.mainapp.calculator.RateCalculator;
import com.murat.mainapp.exception.CalculatorNotFoundException;
import com.murat.mainapp.exception.CurrencyNotFoundException;
import com.murat.mainapp.model.Rate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {
    private RateCalculator rateCalculator;
    private static final Logger logger = LogManager.getLogger(CurrencyService.class);


    public CurrencyService(@Value("${calculator.class}")String className) {
        try{
            Class<?> clazz = Class.forName(className);
            rateCalculator = (RateCalculator) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CalculatorNotFoundException("Couldn't load the calculator class " + className);
        }
    }

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
