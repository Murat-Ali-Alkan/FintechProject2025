package com.murat.mainapp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Rate {


    private String rateName;

    private double bid;

    private double ask;

    private String timestamp;

    private RateStatus status;

    public RateFields toRateFields(){
        return new RateFields(rateName, bid, ask, timestamp, status);
    }


}