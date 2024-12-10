import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String thisClientUsername;

    //Constructor
    public Client(Socket socket, String thisClientUsername) {
        try {
            this.socket = socket;
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.thisClientUsername = thisClientUsername;
        } catch (IOException e) {
            closeEverything(socket, in, out);
        }
    }

    //Sending message to clients and server
    public void sendMessage() {
        //First message sent will be to assign username
        try (Scanner inputScanner = new Scanner(System.in)) {
            out.write(thisClientUsername);
            out.newLine();
            out.flush();

            //Read user's input
            while (socket.isConnected()) {
                String message = inputScanner.nextLine();
                //If user typed /quit, connection is closed
                if (message.equals("/quit")) {
                    out.write("has left the chat.");
                    out.newLine();
                    out.flush();

                    closeEverything(socket, in, out);
                    break;
                }
                //Write the message
                out.write(message);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, in, out);
        }
    }

    //Read user-inputted message (which will be sent)
    public void readMessage() {
        new Thread(() -> {
            String readMessage;

            while (socket.isConnected()) {
                try {
                    readMessage = in.readLine();
                    //Message will be printed
                    System.out.println(readMessage);
                } catch (IOException e) {
                    closeEverything(socket, in, out);
                    break;
                }
            }
        }).start();
    }

    //Close the client's connection
    public void closeEverything(Socket socket, BufferedReader in, BufferedWriter out) {
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


    //Getters
    public BufferedWriter getOut() {
        return out;
    }

    public BufferedReader getIn() {
        return in;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getThisClientUsername() {
        return thisClientUsername;
    }
}
