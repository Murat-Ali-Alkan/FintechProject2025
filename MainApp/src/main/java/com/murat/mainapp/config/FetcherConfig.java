package com.murat.mainapp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class FetcherConfig {

    private String className;
    private String platformName;
    private String userId;
    private String password;
    private String port;
    private List<String> currencyPairs;
    private String baseUrl;

    //Portu implemente et

}