package com.murat.restproducer.controller;

import com.murat.restproducer.model.ExchangeRate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.murat.restproducer.service.ExchangeRateService;

@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();


    @GetMapping("/{currencyPair}")
    public ExchangeRate getExchangeRate(@PathVariable String currencyPair) {
        if(ExchangeRateService.USDTRYUpdates % 3 ==1)
        {
            return exchangeRateService.getRate(currencyPair,true);
        }
        if(ExchangeRateService.EURUSDUpdates % 3 ==1)
        {
            return exchangeRateService.getRate(currencyPair,true);
        }
        return exchangeRateService.getRate(currencyPair,false);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

}
