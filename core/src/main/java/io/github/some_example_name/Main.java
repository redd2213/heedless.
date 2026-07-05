package io.github.some_example_name;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public DatabaseManager database;
    public int currentUserId = -1; //set after login

    @Override
    public void create() {
        database = new DatabaseManager();
        setScreen(new LoginScreen(this));
    }

    @Override
    public void dispose() {
        database.close();
        super.dispose();
    }
}
