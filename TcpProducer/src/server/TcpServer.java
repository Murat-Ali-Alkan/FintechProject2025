package server;

import config.ConfigLoader;
import service.ExchangeRateManager;
import service.SubscriberManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServer {
    private static final int PORT = 8081;
    private static final int UPDATE_INTERVAL = ConfigLoader.getIntProperty("update.interval", 30000);

    /**
     * This variable is used for counting number of updates to make an "abnormal" update
     */
    private static int updateRateCount = 0;

    /**
     * This variable is used for counting number of updates to stop if it reaches max number of updates
     */
    private static int updateCount = 0;

    /**
     * Holds the information about max number of updates
     */
    private static final int NUMBER_OF_UPDATES = ConfigLoader.getIntProperty("update.number", 15);

    /**
     * This method starts the tcp server on the given {@link #PORT}.
     * <p>It also starts {@link #updateRatesPeriodically()} method to simulate a real-life
     * currency application.</p>
     *
     * <p>Takes the connection request of a client, accepts it and then calls {@link ClientHandler}
     * class to handle client requests </p>
     *
     */
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

    /**
     * This method is used for updating rates periodically with the given {@link #UPDATE_INTERVAL}
     * to simulate a real-life
     * currency application.
     *
     * <p>It updates the rates when there is a subscriber.
     * For this purpose it uses {@link SubscriberManager}'s {@code subscriberCount} field</p>
     *
     * <p>Checks the {@link #updateCount} to simulate "abnormal" in specific periods</p>
     *
     * <p>Checks {@link #NUMBER_OF_UPDATES} to make sure it does not exceed the
     * max number of updates
     * </p>
     */
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

