package com.murat.mainapp.callback;

import com.murat.mainapp.model.Rate;
import com.murat.mainapp.model.RateFields;
import com.murat.mainapp.model.RateStatus;

/**
 * This interface is a callback interface for handling rate producers' data-related events such as {@link #onConnect(String, boolean)}, and
 * {@link #onRateAvailable(String, String, Rate)}.
 * <p>
 * Implementations of this interface are expected to handle data-related events received from rate producers,
 * such as establishing or losing connection, receiving new rate information, and updates to existing rates.
 * </p>
 */
public interface PlatformDataCallback {

    /**
     * Called when the connection establishes or fails.
     *
     * @param platformName the name of the platform
     * @param status       {@code true} if connected, {@code false} otherwise
     */
    void onConnect(String platformName, boolean status);

    /**
     * Called when a platform is disconnected.
     *
     * @param platformName the name of the platform
     * @param status       {@code true} if the disconnection was successful, {@code false} otherwise
     */
    void onDisconnect(String platformName, boolean status);

    /**
     * Called when a new rate becomes available from the platform.
     *
     * @param platformName the name of the platform
     * @param rateName     the name of the rate
     * @param rate         the {@link Rate} object representing the full rate details
     */
    void onRateAvailable(String platformName, String rateName, Rate rate);

    /**
     * Called when an existing rate receives new update fields.
     *
     * @param platformName the name of the platform
     * @param rateName     the name of the rate
     * @param rateFields   the updated {@link RateFields} containing new values
     */
    void onRateUpdate(String platformName, String rateName, RateFields rateFields);

    /**
     * Called when the status of a rate changes.
     *
     * @param platformName the name of the platform
     * @param rateName     the name of the rate
     * @param rateStatus   the new {@link RateStatus} of the rate
     */
    void onRateStatus(String platformName, String rateName, RateStatus rateStatus);

}
