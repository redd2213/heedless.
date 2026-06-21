package io.github.some_example_name;

import java.sql.*;

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
}
