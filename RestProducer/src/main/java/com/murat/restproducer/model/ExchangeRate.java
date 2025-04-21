package com.murat.restproducer.model;

/**
 * A simple DTO (Data Transfer Object) that represents an exchange rate.
 *
 * <p>This class contains the core details of an exchange rate including the rate name,
 * bid price, ask price, and the timestamp of when the rate was recorded or updated.</p>
 *
 * <p>Instances of this class are typically used for transferring exchange rate data
 * across different layers of the application and/or between applications.</p>
 */
public class ExchangeRate {
    /**
     * The name of the exchange rate (e.g., "USDTRY")
     */
    private String rateName;
    private double bid;
    private double ask;
    private String timestamp;


    /**
     * Constructs an {@code ExchangeRate} object with the specified properties.
     *
     * @param rateName  the name of the exchange rate (e.g., "USDTRY")
     * @param bid       the bid price
     * @param ask       the ask price
     * @param timestamp the timestamp representing when the rate was updated
     */
    public ExchangeRate(String rateName, double bid, double ask, String timestamp) {
        this.rateName = rateName;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public String getRateName() { return rateName; }
    public double getBid() { return bid; }
    public double getAsk() { return ask; }
    public String getTimestamp() { return timestamp; }
}
