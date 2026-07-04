package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class ReplayScreen implements Screen {
    private Main game;
    private Player player;
    private Enemy enemy;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer layer;

    private Array<InputRecord> records;
    private int recordIndex = 0;
    private float replayTimer = 0f;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean jumpJustPressed = false;
    private boolean attackJustPressed = false;

    public ReplayScreen(Main game, int userId) {
        this.game = game;
        this.records = game.database.loadReplay(userId);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        player = new Player();
        enemy = new Enemy(16, 256, 16, 496);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        replayTimer += delta;

        // process all records up to current time
        while (recordIndex < records.size &&
            records.get(recordIndex).timestamp <= replayTimer) {
            InputRecord record = records.get(recordIndex);

            if (record.keycode == Input.Keys.D) rightPressed = record.pressed;
            if (record.keycode == Input.Keys.A) leftPressed = record.pressed;
            if (record.keycode == Input.Keys.W && record.pressed) jumpJustPressed = true;
            if (record.keycode == Input.Keys.SPACE && record.pressed) attackJustPressed = true;

            recordIndex++;
        }

        // drive player with recorded inputs
        player.updateReplay(delta, layer, leftPressed, rightPressed, jumpJustPressed, attackJustPressed);
        jumpJustPressed = false;
        attackJustPressed = false;

        // enemy update
        enemy.update(delta);

        // draw
        ScreenUtils.clear(Color.BLACK);
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(player.getCurrentFrame(delta),
            player.getX() - (Player.FRAME_WIDTH - 32) / 2f,
            player.getY());
        batch.draw(enemy.getCurrentFrame(),
            enemy.getX() - (Enemy.FRAME_WIDTH - 32) / 2f,
            enemy.getY());
        batch.end();

        // end replay when all records processed
        if (recordIndex >= records.size) {
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        // enemy doesn't have disposable assets for now
    }
}
