import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.*;

//Make this runnable since Client would run it
public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String thisClientUsername;

    //Constructor
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.thisClientUsername = in.readLine();
            clientHandlers.add(this);

            //Handling database fetching for previous messages
            ResultSet resultSet = DatabaseHandler.getPreviousMessages();
            while (resultSet != null && resultSet.next()) {
                String username = resultSet.getString("username");
                String msg = resultSet.getString("message");
                out.write(username + ": " + msg);
                out.newLine();
                out.flush();
            }
            //If a client joins, broadcast that they have joined to all user and server
            broadcastMessage("has joined the chat!");
        } catch (IOException | SQLException e) {
            closeEverything(socket, in, out);
        }
    }

    
    @Override
    public void run() {
        //Client's inputted message
        String clientMessage;
        
        while (socket.isConnected()) {
            try {
                //Message is read from input
                clientMessage = in.readLine();
                if (clientMessage != null) {
                    //If a user left the chat, print out a message for the server
                    if(clientMessage.equals("has left the chat.")){
                        System.out.println("Client " + thisClientUsername + " has left the chat.");
                    }
                    //Else we input it to server's log
                    else{
                        System.out.println("Received from " + thisClientUsername + " : " + clientMessage);
                    }
                    //Broadcast the message to all client and server
                    broadcastMessage(clientMessage);
                }
            } catch (IOException e) {
                closeEverything(socket, in, out);
                break;
            }
        }
    }

    //Handles sending the message to clients and server
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                //If clientHandler corresponds to client
                if (!clientHandler.thisClientUsername.equals(thisClientUsername)) {
                    //If a user left or joins the chat
                    if (message.equals("has left the chat.") || message.equals("has joined the chat!")) {
                        clientHandler.out.write(thisClientUsername + " " + message);
                        DatabaseHandler.saveMessage(thisClientUsername, thisClientUsername + " " + message);
                    } 
                    //If a user typed in normal message
                    else {
                        clientHandler.out.write(thisClientUsername + ": " + message);
                        DatabaseHandler.saveMessage(thisClientUsername, message);
                    }
                    //Newline and flush to maintain message integrity
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

    //Close connection if user quits
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
