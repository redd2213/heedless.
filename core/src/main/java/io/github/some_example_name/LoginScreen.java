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


/** First screen of the application. Displayed after the application is created. */
public class LoginScreen implements Screen {
    private Main game;
    private Stage stage;
    private Skin skin;
    private TextField usernameField;
    private TextField passwordField;
    private Label feedbackLabel;

    public LoginScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Prepare your screen here.
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        Gdx.input.setInputProcessor(stage);

        //table init
        Table table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);

        //title
        Label titleLabel = new Label("Login", skin, "title");
        table.add(titleLabel).padBottom(30).row();

        //username field
        usernameField = new TextField("", skin);
        usernameField.setMessageText("Username");
        table.add(usernameField).width(300).padBottom(10).row();

        //password field
        passwordField = new TextField("", skin);
        passwordField.setMessageText("Password");
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        table.add(passwordField).width(300).padBottom(20).row();

        //feedback label
        feedbackLabel = new Label("", skin);
        table.add(feedbackLabel).padBottom(10).row();

        //login button
        TextButton loginButton = new TextButton("Login", skin);
        table.add(loginButton).width(200).padBottom(10).row();
        loginButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();

                if (username.isEmpty() || password.isEmpty()) {
                    feedbackLabel.setText("Please fill in all fields.");
                    return;
                }

                if (game.database.loginUser(username, password)) {
                    game.setScreen(new MenuScreen(game));
                } else {
                    feedbackLabel.setText("Invalid username or password.");
                }
            }
        });

        //register button
        TextButton registerButton = new TextButton("Register", skin);
        table.add(registerButton).width(200).row();
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

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        ScreenUtils.clear(Color.BLACK);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);

        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
        stage.dispose();
        skin.dispose();
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        stage.dispose();
        skin.dispose();
    }
}
