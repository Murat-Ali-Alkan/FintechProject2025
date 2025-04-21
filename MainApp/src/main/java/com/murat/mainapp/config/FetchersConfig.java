package com.murat.mainapp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration class for holding multiple {@link FetcherConfig} instances.
 * <p>
 * This class is used for multiple platform fetchers.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FetchersConfig {

    /**
     * A list of fetcher configurations, each defining connection and access details
     * for a specific platform.
     */
    private List<FetcherConfig> fetchers;
}
