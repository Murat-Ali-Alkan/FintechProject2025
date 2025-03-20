package com.murat.mainapp.fetcher;

import com.murat.mainapp.callback.PlatformDataCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Component
public class TcpDataFetcher implements  PlatformDataFetcher {

    private static final Logger logger = LogManager.getLogger(TcpDataFetcher.class);

    private int port ;
    private String baseUrl;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private PlatformDataCallback callback;
    private String platformName;
    private String userId;
    private String password;
    private Thread thread;
    private volatile boolean running = false;


    /**
     * @param callback
     */
    @Override
    public void setCallback(PlatformDataCallback callback) {
        this.callback = callback;
    }

    /**
     * @param port
     */
    @Override
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    /**
     * @param baseUrl
     */
    @Override
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * @param platformName
     * @param userId
     * @param password
     */
    @Override
    public void connect(String platformName, String userId, String password) {
        this.platformName = platformName;
        this.userId = userId;
        this.password = password;

        try{
            socket = new Socket("localhost", port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            out.println(userId);
            out.println(password);

            String response = "dummy";

            while(in.readLine() != null){
                response = in.readLine();
                if (response.equalsIgnoreCase("OK")) {
                    break;
                }
            }

            callback.onConnect(platformName, response.equalsIgnoreCase("OK"));

        }catch (Exception e){
            callback.onConnect(platformName,false);
        }


    }

    /**
     * @param platformName
     * @param userId
     * @param password
     */
    @Override
    public void disconnect(String platformName, String userId, String password) {

    }

    /**
     * @param platformName
     * @param rateName
     */
    @Override
    public void subscribe(String platformName, String rateName) {
//        out.println("subscribe|" + rateName);
//        logger.info("subscribed to " + rateName);

    }

    /**
     * @param platformName
     * @param rateName
     */
    @Override
    public void unsubscribe(String platformName, String rateName) {

    }
}

