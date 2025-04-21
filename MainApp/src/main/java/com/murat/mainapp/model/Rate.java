package com.murat.mainapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * A simple DTO (Data Transfer Object) that represents an exchange rate.
 *
 * <p>This class contains the core details of an exchange rate including the rate name,
 * bid price, ask price, and the timestamp of when the rate was recorded or updated.</p>
 *
 * <p>Instances of this class are typically used for transferring exchange rate data
 * across different layers of the application and/or between applications.</p>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Rate implements Serializable {


    private String rateName;

    private double bid;

    private double ask;

    private String timestamp;


    /**
     * This Method transfers the type {@link Rate} to {@link RateFields}
     * @return a new {@link RateFields} instance
     */
    public RateFields toRateFields(){
        return new RateFields(rateName, bid, ask, timestamp);
    }


}