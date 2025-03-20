package com.murat.mainapp.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private RateStatus status;

    public Rate toRate(){
        return  new Rate( rateName, bid, ask, timestamp, status);
    }
}