package server;

import service.SubscriberManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler{
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

     public void handleClient(){
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Connecting To Server");

            out.println("Enter username");
            String username = in.readLine();
            out.println("Enter password");
            String password = in.readLine();

            if("admin".equals(username) && "admin".equals(password)){
                out.println("OK");
            }
            else{
                out.println("Invalid username or password");
                throw new RuntimeException("Invalid username or password");
            }

            out.println("To Exit : \"exit\"");

            String input;
            while ((input = in.readLine()) != null) {
                boolean isExit = handleCommand(input);
                if(isExit){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean handleCommand(String command) {
        boolean isExit = false;
        if (command.startsWith("subscribe|")) {
            String topic = command.substring(10).trim();
            SubscriberManager.subscribe(topic, out);
        } else if (command.startsWith("unsubscribe|")) {
            String topic = command.substring(12).trim();
            SubscriberManager.unsubscribe(topic, out);
        }
        else if (command.equals("exit")) {
            isExit = true;
            try{
                in.close();
                out.close();
            }
            catch(IOException e){
                e.printStackTrace();
            }
        }
        else {
            out.println("ERROR|Invalid request format");
        }
        return isExit;
    }
}
