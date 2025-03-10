package com.murat.restproducer.model;

public class ExchangeRate {
    private String rateName;
    private double bid;
    private double ask;
    private String timestamp;

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
