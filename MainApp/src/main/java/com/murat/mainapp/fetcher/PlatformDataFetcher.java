package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;

public interface PlatformDataFetcher {

    void setCallback(PlatformDataCallback callback);

    void setPort(String port);

    void setBaseUrl(String baseUrl);

    void connect(String platformName, String userId, String password);

    void disconnect(String platformName, String userId, String password);

    void subscribe(String platformName, String rateName);

    void unsubscribe(String platformName, String rateName);

}
