import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String thisClientUsername;

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

    public void sendMessage() {
        try (Scanner inputScanner = new Scanner(System.in)) {
            out.write(thisClientUsername);
            out.newLine();
            out.flush();

            while (socket.isConnected()) {
                String message = inputScanner.nextLine();
                if (message.equals("/quit")) {
                    out.write("has left the chat.");
                    out.newLine();
                    out.flush();

                    closeEverything(socket, in, out);
                    break;
                }
                out.write(message);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, in, out);
        }
    }


    public void readMessage() {
        new Thread(() -> {
            String readMessage;

            while (socket.isConnected()) {
                try {
                    readMessage = in.readLine();
                    System.out.println(readMessage);
                } catch (IOException e) {
                    closeEverything(socket, in, out);
                    break;
                }
            }
        }).start();
    }

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

    public static void main(String[] args) {
        try (Scanner myScanner = new Scanner(System.in)) {
            System.out.print("Enter username: ");
            String myUserName = myScanner.nextLine();
            Socket mySocket = new Socket("localhost", 727);
            Client myClient = new Client(mySocket, myUserName);
            myClient.readMessage();
            myClient.sendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
