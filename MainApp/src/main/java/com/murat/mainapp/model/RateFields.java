package com.murat.mainapp.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * A simple DTO (Data Transfer Object) that represents an exchange rate's fields.
 *
 * <p>This class contains the core details of an exchange rate including the rate name,
 * bid price, ask price, and the timestamp of when the rate was recorded or updated.</p>
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RateFields {

    private String rateName;

    private double bid;

    private double ask;

    private String timestamp;

    /**
     * This Method transfers the type {@link RateFields} to {@link Rate}
     * @return a new {@link Rate} instance
     */
    public Rate toRate(){
        return  new Rate( rateName, bid, ask, timestamp);
    }
}