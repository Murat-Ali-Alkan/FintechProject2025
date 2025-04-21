//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import service.SubscriberManager;

public class ClientHandler {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * This method first authenticates the client.
     * <p>Then it listens for the inputs from the client which it directs them to {@link #handleClient()}
     * method for handling clients requests.</p>
     *
     */
    public void handleClient() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.out.println("Connecting To Server");
            this.out.println("Enter username");
            String username = this.in.readLine();
            this.out.println("Enter password");
            String password = this.in.readLine();
            if (!"admin".equals(username) || !"admin".equals(password)) {
                this.out.println("Invalid username or password");
                throw new RuntimeException("Invalid username or password");
            }
            System.out.println("Client authorized");

            this.out.println("OK");
            this.out.println("To Exit : \"exit\"");

            System.out.println("Listening for new messages");
            String input;
            while(true) {
                if(socket.isClosed()) {
                    break;
                }
                if((input = this.in.readLine()) != null) {
                    System.out.println(input);
                    boolean isExit = this.handleCommand(input);
                    if (isExit) {
                        try {
                            this.in.close();
                            this.out.close();
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method is used for handling clients request like {@code subscribe|PF1_USDTRY}
     * <p>Calls {@link SubscriberManager}'s methods
     * for handling subscribe and unsubscribe requests</p>
     *
     * <p>If the command is {@code "exit"} this method returns true and client connection is closed.</p>
     *
     * @param command is the request which came from client
     * @return boolean to decide if it is an exit request or not
     */
    private boolean handleCommand(String command) {
        boolean isExit = false;
        if (command.startsWith("subscribe|")) {
            String topic = command.substring(10).trim();
            SubscriberManager.subscribe(topic, this.out);
        } else if (command.startsWith("unsubscribe|")) {
            String topic = command.substring(12).trim();
            SubscriberManager.unsubscribe(topic, this.out);
        } else if (command.equals("exit")) {
            isExit = true;


        } else {
            this.out.println("ERROR|Invalid request format");
        }

        return isExit;
    }
}
