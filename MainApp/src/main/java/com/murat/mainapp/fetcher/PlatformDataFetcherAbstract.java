package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class PlatformDataFetcherAbstract {

    protected int port;
    protected String baseUrl;
    protected PlatformDataCallback callback;
    protected String platformName;
    protected String userId;
    protected String password;

    public abstract void setCallback(PlatformDataCallback callback);

    public abstract void setPort(String port);

    public abstract void setBaseUrl(String baseUrl);

    public abstract void connect(String platformName, String userId, String password);

    public abstract void disconnect(String platformName, String userId, String password);

    public abstract void subscribe(String platformName, String rateName);

    public abstract void unsubscribe(String platformName, String rateName);

}
