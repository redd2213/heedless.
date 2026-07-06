package io.github.some_example_name;

import java.sql.*;
import com.badlogic.gdx.utils.Array;

/**
 * Manages all SQLite database operations for Heedless.
 * Handles user registration, authentication, and frame-perfect replay storage.
 * Uses a single persistent connection opened at construction and closed on {@link #close()}.
 *
 * <p>The database contains two tables:
 * <ul>
 *   <li>{@code users} — stores player credentials and stats</li>
 *   <li>{@code frame_states} — stores per-frame game state snapshots for replay playback</li>
 * </ul>
 *
 * <p>All queries use {@link PreparedStatement} to prevent SQL injection attacks.
 */
public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:metroidvania.db";
    private Connection connection;

    /**
     * Creates a new DatabaseManager, establishes the SQLite connection,
     * and ensures all required tables exist.
     */
    public DatabaseManager() {
        connect();
        createTables();
    }

    /**
     * Establishes the connection to the SQLite database file.
     * The database file is created automatically if it does not exist.
     */
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Connected to SQLite database.");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

    /**
     * Creates the {@code users} and {@code frame_states} tables if they do not already exist.
     * Safe to call on every launch due to the {@code CREATE TABLE IF NOT EXISTS} clause.
     */
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

        String createFrameStates = "CREATE TABLE IF NOT EXISTS frame_states (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id INTEGER," +
            "frame_num INTEGER," +
            "timestamp REAL," +
            "delta REAL," +
            "left_held INTEGER," +
            "right_held INTEGER," +
            "jump_pressed INTEGER," +
            "attack_pressed INTEGER," +
            "player_x REAL," +
            "player_y REAL," +
            "velocity_y REAL," +
            "velocity_x REAL," +
            "is_grounded INTEGER," +
            "anim_state TEXT," +
            "facing_right INTEGER," +
            "state_time REAL," +
            "player_health INTEGER," +
            "stone0 INTEGER," +
            "stone1 INTEGER," +
            "stone2 INTEGER," +
            "enemy_x REAL," +
            "enemy_dead INTEGER," +
            "enemy_health INTEGER," +
            "FOREIGN KEY(user_id) REFERENCES users(id)" +
            ");";

        try {
            Statement stmt3 = connection.createStatement();
            stmt3.execute(createFrameStates);
            System.out.println("Frame states table created.");
        } catch (SQLException e) {
            System.err.println("Frame states table failed: " + e.getMessage());
        }
    }

    /**
     * Registers a new user in the database.
     * Fails silently if the username is already taken (UNIQUE constraint).
     *
     * @param username the desired username, must be unique
     * @param password the user's password stored as plain text
     * @return {@code true} if registration succeeded, {@code false} if username already exists
     */
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

    /**
     * Authenticates a user against the database.
     * Uses PreparedStatement to prevent SQL injection.
     *
     * @param username the username to look up
     * @param password the password to verify
     * @return the user's integer ID if credentials match, or {@code -1} if not found
     */
    public int loginUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Login failed: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Closes the database connection.
     * Should be called when the application exits, typically from {@link Main#dispose()}.
     */
    public void close() {
        try {
            if (connection != null) connection.close();
        } catch (SQLException e) {
            System.err.println("Failed to close connection: " + e.getMessage());
        }
    }

    /**
     * Saves a complete replay for the given user to the database.
     * Deletes any existing replay for this user before saving the new one,
     * ensuring only the most recent run is stored.
     *
     * <p>Uses a batch insert inside a single transaction for performance,
     * as replays can contain thousands of frames.
     *
     * @param userId the ID of the user whose replay is being saved
     * @param frames the ordered array of {@link FrameState} snapshots to persist
     */
    public void saveReplay(int userId, Array<FrameState> frames) {
        String sql = "INSERT INTO frame_states (user_id, frame_num, timestamp, delta, " +
            "left_held, right_held, jump_pressed, attack_pressed, " +
            "player_x, player_y, velocity_y, velocity_x, is_grounded, " +
            "anim_state, facing_right, state_time, player_health, " +
            "stone0, stone1, stone2, enemy_x, enemy_dead, enemy_health) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            // delete old replay for this user first
            PreparedStatement del = connection.prepareStatement(
                "DELETE FROM frame_states WHERE user_id = ?");
            del.setInt(1, userId);
            del.executeUpdate();

            // batch insert inside a transaction for speed
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement(sql);

            for (int i = 0; i < frames.size; i++) {
                FrameState fs = frames.get(i);
                stmt.setInt(1, userId);
                stmt.setInt(2, i);
                stmt.setFloat(3, fs.timestamp);
                stmt.setFloat(4, fs.delta);
                stmt.setInt(5, fs.leftHeld ? 1 : 0);
                stmt.setInt(6, fs.rightHeld ? 1 : 0);
                stmt.setInt(7, fs.jumpPressed ? 1 : 0);
                stmt.setInt(8, fs.attackPressed ? 1 : 0);
                stmt.setFloat(9, fs.playerX);
                stmt.setFloat(10, fs.playerY);
                stmt.setFloat(11, fs.velocityY);
                stmt.setFloat(12, fs.velocityX);
                stmt.setInt(13, fs.isGrounded ? 1 : 0);
                stmt.setString(14, fs.animState.name());
                stmt.setInt(15, fs.facingRight ? 1 : 0);
                stmt.setFloat(16, fs.stateTime);
                stmt.setInt(17, fs.playerHealth);
                stmt.setInt(18, fs.stone0 ? 1 : 0);
                stmt.setInt(19, fs.stone1 ? 1 : 0);
                stmt.setInt(20, fs.stone2 ? 1 : 0);
                stmt.setFloat(21, fs.enemyX);
                stmt.setInt(22, fs.enemyDead ? 1 : 0);
                stmt.setInt(23, fs.enemyHealth);
                stmt.addBatch();
            }

            stmt.executeBatch();
            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("Replay saved: " + frames.size + " frames.");
        } catch (SQLException e) {
            System.err.println("Failed to save replay: " + e.getMessage());
            try { connection.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    /**
     * Loads the most recent replay for the given user from the database.
     * Frames are returned in chronological order by frame number.
     *
     * @param userId the ID of the user whose replay should be loaded
     * @return an ordered {@link Array} of {@link FrameState} objects,
     *         or an empty array if no replay exists or an error occurs
     */
    public Array<FrameState> loadReplay(int userId) {
        Array<FrameState> frames = new Array<>();
        String sql = "SELECT * FROM frame_states WHERE user_id = ? ORDER BY frame_num ASC";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                FrameState fs = new FrameState();
                fs.timestamp = rs.getFloat("timestamp");
                fs.delta = rs.getFloat("delta");
                fs.leftHeld = rs.getInt("left_held") == 1;
                fs.rightHeld = rs.getInt("right_held") == 1;
                fs.jumpPressed = rs.getInt("jump_pressed") == 1;
                fs.attackPressed = rs.getInt("attack_pressed") == 1;
                fs.playerX = rs.getFloat("player_x");
                fs.playerY = rs.getFloat("player_y");
                fs.velocityY = rs.getFloat("velocity_y");
                fs.velocityX = rs.getFloat("velocity_x");
                fs.isGrounded = rs.getInt("is_grounded") == 1;
                fs.animState = Player.State.valueOf(rs.getString("anim_state"));
                fs.facingRight = rs.getInt("facing_right") == 1;
                fs.stateTime = rs.getFloat("state_time");
                fs.playerHealth = rs.getInt("player_health");
                fs.stone0 = rs.getInt("stone0") == 1;
                fs.stone1 = rs.getInt("stone1") == 1;
                fs.stone2 = rs.getInt("stone2") == 1;
                fs.enemyX = rs.getFloat("enemy_x");
                fs.enemyDead = rs.getInt("enemy_dead") == 1;
                fs.enemyHealth = rs.getInt("enemy_health");
                frames.add(fs);
            }
            System.out.println("Replay loaded: " + frames.size + " frames.");
        } catch (SQLException e) {
            System.err.println("Failed to load replay: " + e.getMessage());
        }
        return frames;
    }
}
