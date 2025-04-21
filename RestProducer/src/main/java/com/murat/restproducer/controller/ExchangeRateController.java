package com.murat.restproducer.controller;

import com.murat.restproducer.model.ExchangeRate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.murat.restproducer.service.ExchangeRateService;


/**
 * REST controller for managing exchange rates for different currency pairs.
 *
 * <p>This controller provides an endpoint to retrieve the exchange rate for a given currency pair.
 * It uses the {@link ExchangeRateService} to fetch the rate based on predefined logic.</p>
 *
 * @see ExchangeRateService
 * @see ExchangeRate
 */
@RestController
@RequestMapping("/api/rates")
public class ExchangeRateController {
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();


    /**
     * Retrieves the exchange rate for a given currency pair.
     * The response rate may vary based on the update conditions defined in the service.
     *
     * <p>If the update condition for USDTRY or EURUSD (based on the modulus operation) matches,
     * the response will use a "true" flag to modify the rate which generates an abnormal rate. Otherwise, the rate is returned normally.</p>
     *
     * @param currencyPair the currency pair (e.g., "USDTRY", "EURUSD")
     * @return the {@link ExchangeRate} for the given currency pair
     * @throws IllegalArgumentException if the currency pair is invalid or not found
     */
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

    /**
     * Handles {@link IllegalArgumentException} exceptions thrown by this controller.
     *
     * <p>This method ensures that when an invalid currency pair is requested,
     * the response will return a {@link HttpStatus#NOT_FOUND} status code along with the error message.</p>
     *
     * @param ex the exception that was thrown of type {@link IllegalArgumentException}
     * @return a string containing the error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return ex.getMessage();
    }

}
