package com.murat.mainapp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration object for initializing a data fetcher for a specific platform.
 * <p>
 * This class holds the necessary settings required to connect and retrieve data from a platform,
 * such as credentials, supported currency pairs, and the base URL.
 * </p>
 */
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