package io.github.some_example_name;

import java.sql.*;
import com.badlogic.gdx.utils.Array;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:metroidvania.db";
    private Connection connection;

    public DatabaseManager() {
        connect();
        createTables();
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    private void createTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "best_time REAL DEFAULT 0," +
                "playtime_seconds REAL DEFAULT 0" +
        ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(createUsers);
            System.out.println("Tables created succesfully.");
        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
        }

        String createReplays = "CREATE TABLE IF NOT EXISTS replays(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "timestamp REAL," +
            "keycode INTEGER," +
            "pressed INTEGER," +
            "completion_time REAL," +
            "FOREIGN KEY(user_id) REFERENCES users(id)" +
        ");";

        try {
            Statement stmt = connection.createStatement();
            stmt.execute(createReplays);
            System.out.println("Replay created succesfully.");
        } catch (SQLException e) {
            System.err.println("Replay creation failed: " + e.getMessage());
        }
    }

    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean loginUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }

    public void saveInputRecord(int userId, float timestamp,
                                int keycode, boolean pressed,
                                float completionTime) {
        String sql = "INSERT INTO replays (user_id, timestamp, keycode, pressed, completion_time) " +
            "VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setFloat(2, timestamp);
            stmt.setInt(3, keycode);
            stmt.setInt(4, pressed ? 1 : 0);
            stmt.setFloat(5, completionTime);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save input: " + e.getMessage());
        }
    }

    public Array<InputRecord> loadReplay(int userId) {
        Array<InputRecord> records = new Array<>();
        String sql = "SELECT timestamp, keycode, pressed FROM replays " +
            "WHERE user_id = ? ORDER BY timestamp ASC";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(new InputRecord(
                    rs.getFloat("timestamp"),
                    rs.getInt("keycode"),
                    rs.getInt("pressed") == 1
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to load replay: " + e.getMessage());
        }
        return records;
    }
}
