package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/** First screen of the application. Displayed after the application is created. */
public class GameScreen implements Screen {
    private SpriteBatch batch;
    private Player player;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer layer;
    private Main game;
    private Enemy enemy;
    private OrthographicCamera hudCamera;
    private Stage hudStage;
    private Label healthLabel;
    private Skin skin;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Prepare your screen here.
        //skin init
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));

        //player init
        batch = new SpriteBatch();
        player = new Player();

        //fixes working buttons even after screen swap
        Gdx.input.setInputProcessor(null);

        // camera setup
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //map import and init
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");

        // enemy init
        enemy = new Enemy(16, 256, 16, 496);

        //hud camera setup
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //hud init
        hudStage = new Stage(new ScreenViewport());
        healthLabel = new Label("HP: " + player.getCurrentHealth(), skin);
        healthLabel.setPosition(20, Gdx.graphics.getHeight() -40);
        hudStage.addActor(healthLabel);
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.

        //WASD inputs
        player.update(delta, layer);

        //enemy
        if (!enemy.isDead()) {
            enemy.update(delta);
        }

        // player hit by enemy
        if(!enemy.isDead() && player.getBounds().overlaps(enemy.getBounds())) {
            float direction = player.getX() > enemy.getX() ? 1 : -1;
            player.applyKnockback(direction);
            player.takeDamage(1);
        }

        //enemy hit by player
        if (player.isAttacking() && player.getAttackHitbox().overlaps(enemy.getBounds())) {
            enemy.takeDamage(1);
        }

        // death check
        if (player.isDead()) {
            game.setScreen(new GameOverScreen(game));
        }

        // win check (temp)
        if (enemy.isDead()) {
            game.setScreen(new WinScreen(game));
        }

        ScreenUtils.clear(Color.BLACK);


        //camera, map and player
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //draw for player and enemy
        batch.draw(player.getCurrentFrame(delta), player.getX() - (Player.FRAME_WIDTH -32) /2f, player.getY());

        if (!enemy.isDead()) {
            batch.draw(enemy.getCurrentFrame(),
                enemy.getX() - (Enemy.FRAME_WIDTH -32)/ 2f,
                enemy.getY());
        }

        batch.end();

        //hud health
        healthLabel.setText("HP: " + player.getCurrentHealth() + "/ 5");
        hudStage.act(delta);
        hudStage.draw();

    }

    @Override
    public void resize(int width, int height) {
        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
        if(width <= 0 || height <= 0) return;

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
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        hudStage.dispose();
        skin.dispose();
    }
}
