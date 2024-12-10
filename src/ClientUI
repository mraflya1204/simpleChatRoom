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

        // Send the username to the server
        try {
            client.getOut().write(client.getThisClientUsername());
            client.getOut().newLine();
            client.getOut().flush();
        } catch (IOException e) {
            showError("Error sending username to the server.");
            e.printStackTrace();
        }

        // Set up the main frame
        frame = new JFrame("Chat Room");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Chat display area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Send");
        quitButton = new JButton("Quit");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(quitButton, BorderLayout.WEST);

        frame.add(inputPanel, BorderLayout.SOUTH);

        // Event listeners
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quitChat();
            }
        });

        // Display the frame
        frame.setVisible(true);

        // Start listening for incoming messages
        startListeningForMessages();
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.trim().isEmpty()) {
            try {
                // Send the message to the server
                client.getOut().write(message);
                client.getOut().newLine();
                client.getOut().flush();

                // Display the sent message in the chat area with the username
                chatArea.append(client.getThisClientUsername() + ": " + message + "\n");

                // Clear the input field
                messageField.setText("");
            } catch (IOException e) {
                showError("Error sending message.");
                e.printStackTrace();
            }
        }
    }

    private void quitChat() {
        try {
            // Signal the listening thread to stop
            isRunning = false;

            // Notify the server about quitting
            client.getOut().write("has left the chat.");
            client.getOut().newLine();
            client.getOut().flush();

            // Close resources
            client.closeEverything(client.getSocket(), client.getIn(), client.getOut());

            // Close the UI
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

    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            String username = JOptionPane.showInputDialog("Enter your username:");
            if (username != null && !username.trim().isEmpty()) {
                // Attempt to connect to the server
                Socket socket = new Socket("localhost", 727);
                Client client = new Client(socket, username);
                new ClientUI(client);
            } else {
                System.out.println("Username is required to join the chat.");
            }
        } catch (IOException e) {
            // Display error window if server is not available
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to the server. Please ensure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
