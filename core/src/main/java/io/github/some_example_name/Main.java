package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/**
 * Main application entry point for Heedless.
 * Extends {@link Game} to manage screen transitions throughout the application lifecycle.
 * Holds global state that must persist across screen changes, including the database
 * connection, the currently logged-in user ID, and the persistent menu music track.
 */
public class Main extends Game {

    /** The database manager instance used across all screens for user and replay data. */
    public DatabaseManager database;

    /**
     * The ID of the currently logged-in user.
     * Set to -1 until a user successfully authenticates via {@link LoginScreen}.
     */
    public int currentUserId = -1;

    /**
     * The persistent menu music track that plays across Login, Menu,
     * Cutscene and Story screens. Stored here so it survives screen transitions.
     */
    public Music menuMusic;

    /**
     * Called once when the application starts.
     * Initializes the database, starts the menu music, and navigates to the login screen.
     */
    @Override
    public void create() {
        database = new DatabaseManager();
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal(
            "Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Rest Area(MenuScreen).wav"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();
        setScreen(new LoginScreen(this));
    }

    /**
     * Called when the application is closing.
     * Stops and disposes the menu music and closes the database connection.
     */
    @Override
    public void dispose() {
        menuMusic.stop();
        menuMusic.dispose();
        database.close();
        super.dispose();
    }
}
