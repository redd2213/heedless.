package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The login and registration screen for Heedless.
 * Allows existing users to authenticate and new users to create an account.
 * On successful login, stores the user's ID in {@link Main#currentUserId}
 * and navigates to {@link MenuScreen}.
 */
public class LoginScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private TextField passwordField;
    private Label feedbackLabel;

    /**
     * Creates a new LoginScreen.
     *
     * @param game the main game instance used for screen navigation and database access
     */
    public LoginScreen(Main game) {
        this.game = game;
    }

    /**
     * Initializes the login form with username/password fields,
     * login and register buttons, and a feedback label for error messages.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        // title
        Label titleLabel = new Label("Login", skin, "title");
        table.add(titleLabel).padBottom(30).row();

        // username field
        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(500).padBottom(10).row();

        // password field
        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(500).padBottom(20).row();

        // feedback label — shows success or error messages
        feedbackLabel = new Label("", skin);
        table.add(feedbackLabel).padBottom(10).row();

        // login button
        TextButton loginButton = new TextButton("Login", skin);
        table.add(loginButton).size(300, 100).padBottom(40).row();
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    feedbackLabel.setText("Please fill in all fields.");
                    return;
                }

                int userId = game.database.loginUser(username, password);
                if (userId != -1) {
                    game.currentUserId = userId;
                    game.setScreen(new MenuScreen(game));
                } else {
                    feedbackLabel.setText("Invalid username or password.");
                }
            }
        });

        // register button
        TextButton registerButton = new TextButton("Register", skin);
        table.add(registerButton).size(300, 100).row();
        registerButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    feedbackLabel.setText("Please fill in all fields.");
                    return;
                }

                if (game.database.registerUser(username, password)) {
                    feedbackLabel.setText("Registered! You can now login.");
                } else {
                    feedbackLabel.setText("Username already taken.");
                }
            }
        });
    }

    /**
     * Clears the screen and renders the login form each frame.
     *
     * @param delta time elapsed since last frame in seconds
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    /**
     * Updates the stage viewport on window resize.
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    /**
     * Disposes of stage and skin assets to free memory.
     */
    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
