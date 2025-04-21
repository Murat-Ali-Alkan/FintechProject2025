package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;

/**
 * Interface for fetching platform-specific data and handling subscription-based data streams.
 * <p>
 * Implementations of this interface are responsible for:
 * <ul>
 *     <li>Managing connection and disconnection to a data platform</li>
 *     <li>Subscribing and unsubscribing to specific data feeds (e.g., rate names)</li>
 *     <li>Configuring communication parameters such as port and base URL</li>
 *     <li>Relaying data through callbacks using {@link PlatformDataCallback}</li>
 * </ul>
 * </p>
 */
public interface PlatformDataFetcher {

    /**
     * Sets the callback interface to be used for data events and connection updates.
     *
     * @param callback the callback instance implementing {@link PlatformDataCallback}
     */
    void setCallback(PlatformDataCallback callback);

    /**
     * Sets the communication port for the data platform.
     *
     * @param port the port to be used for connections
     */
    void setPort(String port);

    /**
     * Sets the base URL for the platform's API or data endpoint.
     *
     * @param baseUrl the base URL of the data source
     */
    void setBaseUrl(String baseUrl);

    /**
     * Connects to the data platform using the specified credentials.
     *
     * @param platformName the name of the platform to connect to
     * @param userId       the user ID for authentication
     * @param password     the password for authentication
     */
    void connect(String platformName, String userId, String password);

    /**
     * Disconnects from the data platform.
     *
     * @param platformName the name of the platform to disconnect from
     * @param userId       the user ID used during connection
     * @param password     the password used during connection
     */
    void disconnect(String platformName, String userId, String password);

    /**
     * Subscribes to a specific data feed on the platform.
     *
     * @param platformName the platform providing the feed
     * @param rateName     the name of the rate or data feed to subscribe to
     */
    void subscribe(String platformName, String rateName);

    /**
     * Unsubscribes from a specific data feed on the platform.
     *
     * @param platformName the platform providing the feed
     * @param rateName     the name of the rate or data feed to unsubscribe from
     */
    void unsubscribe(String platformName, String rateName);

}
