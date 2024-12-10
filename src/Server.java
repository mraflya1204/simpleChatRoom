import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;

    //Constructor
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }
        
    public void startServer() {
        //Create thread to listen for commands from clients
        new Thread(this::listenForCommands).start();
    
        try {
            //If server is currently running, if a client joins, output a message in the Server's terminal
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("A client has connected to the server.");
                //Create a clientHandler
                ClientHandler clientHandler = new ClientHandler(socket);

                //Create and start a thread to handle the clientHandler assigned to the newly joined client
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listenForCommands() {
        //Scanner for server-side command
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String command = scanner.nextLine();
            //If user typed in /flush in server terminal, it will reset the database
            if (command.equalsIgnoreCase("/flush")) {
                System.out.println("Flushing all messages from the database...");
                DatabaseHandler.flushMessages();
                System.out.println("All messages have been deleted.");
            }
        }
    }
    
    public void closeServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        try {
            //Create server socket and start the server on that socket
            ServerSocket serverSocket = new ServerSocket(727);
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
