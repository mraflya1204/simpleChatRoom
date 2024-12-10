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

        try {
            client.getOut().write(client.getThisClientUsername());
            client.getOut().newLine();
            client.getOut().flush();
        } catch (IOException e) {
            showError("Error sending username to the server.");
            e.printStackTrace();
        }

        frame = new JFrame("Chat Room");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());

        messageField = new JTextField();
        sendButton = new JButton("Send");
        quitButton = new JButton("Quit");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.add(quitButton, BorderLayout.WEST);

        frame.add(inputPanel, BorderLayout.SOUTH);

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

        frame.setVisible(true);

        startListeningForMessages();
    }

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

    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        try {
            String username = JOptionPane.showInputDialog("Enter your username:");
            if (username != null && !username.trim().isEmpty()) {
                Socket socket = new Socket("localhost", 727);
                Client client = new Client(socket, username);
                new ClientUI(client);
            } else {
                System.out.println("Username is required to join the chat.");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Unable to connect to the server. Please ensure the server is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
