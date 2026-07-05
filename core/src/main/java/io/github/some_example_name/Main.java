package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public DatabaseManager database;
    public int currentUserId = -1; //set after login
    public Music menuMusic;

    @Override
    public void create() {
        database = new DatabaseManager();
        setScreen(new LoginScreen(this));
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal(
            "Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Rest Area(MenuScreen).wav"));
        menuMusic.setLooping(true);
        menuMusic.setVolume(0.5f);
        menuMusic.play();
    }

    @Override
    public void dispose() {
        menuMusic.stop();
        menuMusic.dispose();
        database.close();
        super.dispose();
    }
}
