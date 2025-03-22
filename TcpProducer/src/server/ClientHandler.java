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

            this.out.println("OK");
            this.out.println("To Exit : \"exit\"");

            String input;
            while((input = this.in.readLine()) != null) {
                boolean isExit = this.handleCommand(input);
                if (isExit) {
                    break;
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

            try {
                this.in.close();
                this.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            this.out.println("ERROR|Invalid request format");
        }

        return isExit;
    }
}
