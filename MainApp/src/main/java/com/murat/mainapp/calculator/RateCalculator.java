package com.murat.mainapp.calculator;

import com.murat.mainapp.model.Rate;

public interface RateCalculator {
    Rate calculateUSDTRY(Rate rate1, Rate rate2);

    Rate calculateEURTRY(Rate rateUSDTRY , Rate rate1, Rate rate2);

    Rate calculateGBPTRY(Rate rateGBPTRY, Rate rate1, Rate rate2);
}
