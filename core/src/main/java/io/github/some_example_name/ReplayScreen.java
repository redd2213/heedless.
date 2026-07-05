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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class ReplayScreen implements Screen {
    private Main game;
    private Player player;
    private Enemy enemy;
    private Collectible[] collectibles;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer layer;

    // HUD
    private Stage hudStage;
    private Skin skin;
    private Label healthLabel;
    private Label collectiblesLabel;

    // Replay data
    private Array<FrameState> frames;
    private int frameIndex = 0;

    public ReplayScreen(Main game, int userId) {
        this.game = game;
        this.frames = game.database.loadReplay(userId);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        // Player starts at default position — state will be restored from frame 0
        player = new Player();

        // Enemy same patrol as GameScreen
        enemy = new Enemy(16, 256, 16, 496);

        // Collectibles - Using the exact Tiled coordinates we fixed earlier!
        collectibles = new Collectible[] {
            new Collectible(752, 176),
            new Collectible(112, 432),
            new Collectible(464, 400)
        };

        // Camera - Including the Zoom fix!
        camera = new OrthographicCamera();
        float zoomFactor = 2f;
        camera.setToOrtho(false, Gdx.graphics.getWidth() / zoomFactor, Gdx.graphics.getHeight() / zoomFactor);

        // Map
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");

        // HUD
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        hudStage = new Stage(new ScreenViewport());

        healthLabel = new Label("HP: 5/5", skin);
        healthLabel.setPosition(20, Gdx.graphics.getHeight() - 40);
        hudStage.addActor(healthLabel);

        collectiblesLabel = new Label("Stones: 0/3", skin);
        collectiblesLabel.setPosition(20, Gdx.graphics.getHeight() - 70);
        hudStage.addActor(collectiblesLabel);

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        // End of replay — go back to menu
        if (frameIndex >= frames.size) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        FrameState fs = frames.get(frameIndex);
        frameIndex++;

        // === RESTORE STATE DIRECTLY — NO PHYSICS SIMULATION ===
        player.restoreState(fs);

        if (!fs.enemyDead) {
            enemy.restoreState(fs);
        }

        // Restore collectibles based on the frame data
        int collectedCount = 0;
        if (fs.stone0) collectibles[0].collect();
        if (fs.stone1) collectibles[1].collect();
        if (fs.stone2) collectibles[2].collect();

        if (collectibles[0].isCollected()) collectedCount++;
        if (collectibles[1].isCollected()) collectedCount++;
        if (collectibles[2].isCollected()) collectedCount++;

        // === DRAW ===
        ScreenUtils.clear(Color.BLACK);
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw uncollected collectibles (Updated for animations!)
        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                c.update(fs.delta); // Progress the animation timer
                batch.draw(c.getCurrentFrame(), c.getX(), c.getY(), Collectible.SIZE, Collectible.SIZE);
            }
        }

        // Draw player using recorded animation state
        batch.draw(player.getCurrentFrameForReplay(),
            player.getX() - (Player.FRAME_WIDTH - 32) / 2f,
            player.getY());

        // Draw enemy only if alive in this frame
        if (!fs.enemyDead) {
            enemy.advanceAnimation(fs.delta);
            batch.draw(enemy.getCurrentFrame(),
                enemy.getX() - (Enemy.FRAME_WIDTH - 32) / 2f,
                enemy.getY());
        }

        batch.end();

        // HUD Updates
        healthLabel.setText("HP: " + fs.playerHealth + " / 5");
        collectiblesLabel.setText("Stones: " + collectedCount + " / 3");
        hudStage.act(fs.delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        // Keep zoom consistent on resize
        float zoomFactor = 2f;
        camera.setToOrtho(false, width / zoomFactor, height / zoomFactor);
        hudStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        hudStage.dispose();
        skin.dispose();
        for (Collectible c : collectibles) c.dispose();
    }
}
