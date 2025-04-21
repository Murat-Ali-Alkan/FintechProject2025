package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;
import lombok.Getter;
import lombok.Setter;

/**
 * Abstract class for fetching platform-specific data and handling subscription-based data streams.
 * <p>
 * Children of this Abstract class are responsible for:
 * <ul>
 *     <li>Managing connection and disconnection to a data platform</li>
 *     <li>Subscribing and unsubscribing to specific data feeds (e.g., rate names)</li>
 *     <li>Configuring communication parameters such as port and base URL</li>
 *     <li>Relaying data through callbacks using {@link PlatformDataCallback}</li>
 * </ul>
 * </p>
 */
@Getter
@Setter
public abstract class PlatformDataFetcherAbstract {

    protected int port;
    protected String baseUrl;
    protected PlatformDataCallback callback;
    protected String platformName;
    protected String userId;
    protected String password;

    /**
     * Sets the callback interface to be used for data events and connection updates.
     *
     * @param callback the callback instance implementing {@link PlatformDataCallback}
     */
    public abstract void setCallback(PlatformDataCallback callback);

    /**
     * Sets the communication port for the data platform.
     *
     * @param port the port to be used for connections
     */
    public abstract void setPort(String port);

    /**
     * Sets the base URL for the platform's API or data endpoint.
     *
     * @param baseUrl the base URL of the data source
     */
    public abstract void setBaseUrl(String baseUrl);

    /**
     * Connects to the data platform using the specified credentials.
     *
     * @param platformName the name of the platform to connect to
     * @param userId       the user ID for authentication
     * @param password     the password for authentication
     */
    public abstract void connect(String platformName, String userId, String password);

    /**
     * Disconnects from the data platform.
     *
     * @param platformName the name of the platform to disconnect from
     * @param userId       the user ID used during connection
     * @param password     the password used during connection
     */
    public abstract void disconnect(String platformName, String userId, String password);

    /**
     * Subscribes to a specific data feed on the platform.
     *
     * @param platformName the platform providing the feed
     * @param rateName     the name of the rate or data feed to subscribe to
     */
    public abstract void subscribe(String platformName, String rateName);

    /**
     * Unsubscribes from a specific data feed on the platform.
     *
     * @param platformName the platform providing the feed
     * @param rateName     the name of the rate or data feed to unsubscribe from
     */
    public abstract void unsubscribe(String platformName, String rateName);

}
