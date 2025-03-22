package com.murat.mainapp.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RateFields {

    private String rateName;

    private double bid;

    private double ask;

    private String timestamp;


    public Rate toRate(){
        return  new Rate( rateName, bid, ask, timestamp);
    }
}