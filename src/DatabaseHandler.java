import java.sql.*;

public class DatabaseHandler {
    //Database information (postgresql)
    private static final String URL = "jdbc:postgresql://localhost:5432/minimalChatRoom";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    //Get connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    //Save a client-sent message to the database
    public static void saveMessage(String username, String message) {
        //?, ? represents a prepared statement
        String query = "INSERT INTO chat_messages (username, message) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, message);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Get previously sent message in chat room from the database
    public static ResultSet getPreviousMessages() {
        String query = "SELECT username, message, timestamp FROM chat_messages ORDER BY timestamp ASC";
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Deletes the data if server uses /flush command
    public static void flushMessages() {
        String query = "DELETE FROM chat_messages";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
            System.out.println("All messages have been flushed from the database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
