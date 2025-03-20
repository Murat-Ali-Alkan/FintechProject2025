package com.murat.mainapp.callback;

import com.murat.mainapp.model.Rate;
import com.murat.mainapp.model.RateFields;
import com.murat.mainapp.model.RateStatus;

public interface PlatformDataCallback {

    void onConnect(String platformName, boolean status);
    void onDisconnect(String platformName, boolean status);
    void onRateAvailable(String platformName, String rateName, Rate rate);
    void onRateUpdate(String platformName, String rateName, RateFields rateFields);
    void onRateStatus(String platformName, String rateName, RateStatus rateStatus);

}
