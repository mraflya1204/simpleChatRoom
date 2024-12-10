import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;

public class ClientUI {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton quitButton;
    private Client client;
    private volatile boolean isRunning = true;


    public ClientUI(Client client) {
        this.client = client;

        //Send username to the server
        try {
            client.getOut().write(client.getThisClientUsername());
            client.getOut().newLine();
            client.getOut().flush();
        } catch (IOException e) {
            showError("Error sending username to the server.");
            e.printStackTrace();
        }

        //New window
        frame = new JFrame("Chat Room");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        //Chat area to show the messages from yourself and other users
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        //Main input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        //Buttons
        //Input text here
        messageField = new JTextField();
        //Send will send the inputted message from inputPanel
        sendButton = new JButton("Send");
        //Quit will remove the connection to the server and quit
        quitButton = new JButton("Quit");

        //button and messageField Placements
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(quitButton, BorderLayout.WEST);

        //Add border
        frame.add(inputPanel, BorderLayout.SOUTH);

        //ActionListener if user clicks send
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        //ActionListener if user pressed enter on the text input 
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        //ActionListener if user pressed quit button
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitChat();
            }
        });

        //Make the window visible
        frame.setVisible(true);

        //Listen to Message
        startListeningForMessages();
    }

    //Mostly the same as Client's implementation
    private void sendMessage() {
        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            try {
                client.getOut().write(message);
                client.getOut().newLine();
                client.getOut().flush();

                chatArea.append(client.getThisClientUsername() + ": " + message + "\n");

                messageField.setText("");
            } catch (IOException e) {
                showError("Error sending message.");
                e.printStackTrace();
            }
        }
    }

    private void quitChat() {
        try {
            isRunning = false;

            client.getOut().write("has left the chat.");
            client.getOut().newLine();
            client.getOut().flush();

            client.closeEverything(client.getSocket(), client.getIn(), client.getOut());

            frame.dispose();
        } catch (IOException e) {
            showError("Error while quitting the chat.");
            e.printStackTrace();
        }
    }

    private void startListeningForMessages() {
        new Thread(() -> {
            String message;
            try {
                while (isRunning && (message = client.getIn().readLine()) != null) {
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                if (isRunning) { // Only show the error if the thread wasn't intentionally stopped
                    showError("Connection lost, server is down.");
                    e.printStackTrace();
                }
            }
        }).start();
    }

    //If there is an error, show a new window saying an error message
    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }
        
    public static void main(String[] args) {
        try {
            //Create a window to enter username
            String username = JOptionPane.showInputDialog("Enter your username:");
            if (username != null && !username.trim().isEmpty()) {
                //Establish connection if Username is valid
                Socket socket = new Socket("localhost", 727);
                Client client = new Client(socket, username);
                new ClientUI(client);
            } else {
                System.out.println("Username is required to join the chat.");
            }
        } catch (IOException e) {
            //Error messages
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to the server. Please ensure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
