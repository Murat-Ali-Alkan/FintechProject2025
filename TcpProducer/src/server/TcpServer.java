package server;

import config.ConfigLoader;
import service.ExchangeRateManager;
import service.SubscriberManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static final int PORT = 8081;
    private static final int UPDATE_INTERVAL = ConfigLoader.getIntProperty("update.interval", 5000);
    private static int updateRateCount = 0;
    private static int updateCount = 0;
    private static final int NUMBER_OF_UPDATES = ConfigLoader.getIntProperty("update.number", 15);

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP Server listens to port 8081");

            new Thread(this::updateRatesPeriodically).start();

            while (true) {
                try(Socket clientSocket = serverSocket.accept()) {
                    System.out.println("Client connected");
                    new ClientHandler(clientSocket).handleClient();
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateRatesPeriodically() {
        while (updateCount < NUMBER_OF_UPDATES) {
            try{
                Thread.sleep(UPDATE_INTERVAL);
                if(SubscriberManager.subscriberCount != 0) {

                    if(updateRateCount == 3) {
                        ExchangeRateManager.updateLargeRates();
                        SubscriberManager.notifySubscribers(true);
                        updateRateCount = 0;
                    }
                    else {
                        SubscriberManager.notifySubscribers(false);
                        ExchangeRateManager.updateRates();
                        updateRateCount++;
                    }
                    updateCount++;
                }
            }catch (InterruptedException e) {
                    e.printStackTrace();

            }

        }

    }
}

