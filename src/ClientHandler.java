import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.*;


public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String thisClientUsername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.thisClientUsername = in.readLine();
            clientHandlers.add(this);

            // Send previous messages
            ResultSet resultSet = DatabaseHandler.getPreviousMessages();
            while (resultSet != null && resultSet.next()) {
                String username = resultSet.getString("username");
                String msg = resultSet.getString("message");
                out.write(username + ": " + msg);
                out.newLine();
                out.flush();
            }

            broadcastMessage("has joined the chat!");
        } catch (IOException | SQLException e) {
            closeEverything(socket, in, out);
        }
    }


    @Override
    public void run() {
        String clientMessage;

        while (socket.isConnected()) {
            try {
                clientMessage = in.readLine();
                if (clientMessage != null) {
                    if(clientMessage.equals("has left the chat.")){
                        System.out.println("Client " + thisClientUsername + " has left the chat.");
                    }
                    else{
                        System.out.println("Received from " + thisClientUsername + " : " + clientMessage);
                    }
                    broadcastMessage(clientMessage);
                }
            } catch (IOException e) {
                closeEverything(socket, in, out);
                break;
            }
        }
    }


    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.thisClientUsername.equals(thisClientUsername)) {
                    if (message.equals("has left the chat.") || message.equals("has joined the chat!")) {
                        clientHandler.out.write(thisClientUsername + " " + message);
                        DatabaseHandler.saveMessage(thisClientUsername, thisClientUsername + " " + message);
                    } else {
                        clientHandler.out.write(thisClientUsername + ": " + message);
                        DatabaseHandler.saveMessage(thisClientUsername, message);
                    }
                    clientHandler.out.newLine();
                    clientHandler.out.flush();
                }
            } catch (IOException e) {
                closeEverything(clientHandler.socket, clientHandler.in, clientHandler.out);
            }
        }
    }



    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

    public void closeEverything(Socket socket, BufferedReader in, BufferedWriter out) {
        removeClientHandler();
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}