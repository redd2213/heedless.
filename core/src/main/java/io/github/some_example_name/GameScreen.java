package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * The main gameplay screen for Heedless.
 * Manages the complete game loop including player input and physics,
 * enemy AI, tile collision, collectible pickup, portal activation,
 * HUD rendering, pause functionality, and the frame state recording system.
 *
 * <p>Win condition: all three Energy Stones collected AND the enemy defeated
 * AND the player enters the activated portal.
 *
 * <p>The screen uses a zoomed {@link OrthographicCamera} for the game world
 * and a separate {@link Stage} for the fixed HUD overlay.
 */
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
    private Label pauseLabel;
    private Label objectiveLabel;
    private Skin skin;
    private FrameRecorder recorder;
    private int currentUserId;
    private boolean paused = false;
    private Collectible[] collectibles;
    private int collectedCount = 0;
    private boolean allCollected = false;
    private Label collectiblesLabel;
    private Texture attackTexture;
    private Label attackLabel;
    private Portal portal;
    private Animation<TextureRegion> girlAnim;
    private float girlStateTime = 0f;
    private static final float GIRL_X = 590f;
    private static final float GIRL_Y = 367f;
    private Music music;

    /**
     * Creates a new GameScreen.
     *
     * @param game the main game instance used for screen navigation and database access
     */
    public GameScreen(Main game) {
        this.game = game;
    }

    /**
     * Initializes all game objects, assets, camera, map, HUD, and recording system.
     * Stops the menu music and starts the dungeon music track.
     */
    @Override
    public void show() {
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        batch = new SpriteBatch();

        // girl animation — dancing next to the portal
        Texture girlSheet = new Texture(Gdx.files.internal(
            "Assets/SPRITES/Dancing Girl Files/spritesheets/snap.png"));
        TextureRegion[][] girlTmp = TextureRegion.split(girlSheet,
            girlSheet.getWidth() / 8, girlSheet.getHeight());
        Array<TextureRegion> girlFrames = new Array<>();
        for (int i = 0; i < 8; i++) girlFrames.add(girlTmp[0][i]);
        girlAnim = new Animation<>(0.1f, girlFrames);

        // music — stop menu music, start dungeon track
        music = Gdx.audio.newMusic(Gdx.files.internal(
            "Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Dungeon(GameScreen).wav"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();
        if (game.menuMusic != null && game.menuMusic.isPlaying()) {
            game.menuMusic.stop();
        }

        Gdx.input.setInputProcessor(null);

        // camera with zoom to scale pixel art appropriately
        float zoomFactor = 1.4f;
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() / zoomFactor,
            Gdx.graphics.getHeight() / zoomFactor);

        // map
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");

        // game objects
        player = new Player();
        enemy = new Enemy(16, 256, 16, 496);

        collectibles = new Collectible[] {
            new Collectible(745, 176),
            new Collectible(105, 432),
            new Collectible(460, 400)
        };

        portal = new Portal(656, 360);

        // HUD camera — fixed, never moves
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // HUD stage
        hudStage = new Stage(new ScreenViewport());

        healthLabel = new Label("HP: " + player.getCurrentHealth(), skin, "title");
        healthLabel.setFontScale(0.5f);
        healthLabel.setPosition(20, Gdx.graphics.getHeight() - 100);
        hudStage.addActor(healthLabel);

        collectiblesLabel = new Label("Stones : 0/3", skin, "title");
        collectiblesLabel.setFontScale(0.5f);
        collectiblesLabel.setPosition(20, Gdx.graphics.getHeight() - 160);
        hudStage.addActor(collectiblesLabel);

        Table objectiveTable = new Table();
        objectiveTable.setFillParent(true);
        objectiveTable.bottom().padBottom(50);
        Label objectiveLabel = new Label(
            "Objective: Collect all the Energy Stones and slay all the monsters " +
                "to unlock the Portal to the next room!", skin);
        objectiveTable.add(objectiveLabel);
        hudStage.addActor(objectiveTable);

        // attack tutorial — fades in then out automatically
        Texture attackTexture = new Texture(Gdx.files.internal(
            "Assets/SPRITES/player/Punch/sprites/f-02.png"));
        Image attackImage = new Image(attackTexture);
        Table tutorialTable = new Table();
        tutorialTable.setFillParent(true);
        tutorialTable.bottom().padBottom(150);
        tutorialTable.add(attackImage).size(
            attackTexture.getWidth() * 2f, attackTexture.getHeight() * 2f);
        Label attackLabel = new Label("= SPACE", skin);
        tutorialTable.add(attackLabel).padLeft(10).padTop(50);
        tutorialTable.getColor().a = 0f;
        tutorialTable.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(4f),
            Actions.fadeOut(1f),
            Actions.removeActor()
        ));
        hudStage.addActor(tutorialTable);

        // pause overlay
        Table pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();
        pauseLabel = new Label("PAUSED", skin, "title");
        pauseLabel.setVisible(false);
        pauseTable.add(pauseLabel);
        hudStage.addActor(pauseTable);

        // replay recorder
        currentUserId = game.currentUserId;
        recorder = new FrameRecorder();
        recorder.startRecording();
    }

    /**
     * The main game loop. Processes input, updates all game objects,
     * checks win/lose conditions, records frame state, and renders everything.
     * When paused, only the HUD is rendered and updated.
     *
     * @param delta time elapsed since last frame in seconds
     */
    @Override
    public void render(float delta) {
        // signal single-frame events to recorder before player update
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && player.isGrounded()) {
            recorder.onJumpPressed();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !player.isAttacking()) {
            recorder.onAttackPressed();
        }

        // pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            pauseLabel.setVisible(paused);
        }

        // freeze everything when paused, only draw HUD
        if (paused) {
            hudStage.act(delta);
            hudStage.draw();
            return;
        }

        // update logic
        player.update(delta, layer);
        if (!enemy.isDead()) enemy.update(delta);

        // player hit by enemy
        if (!enemy.isDead() && player.getBounds().overlaps(enemy.getBounds())) {
            float direction = player.getX() > enemy.getX() ? 1 : -1;
            player.applyKnockback(direction);
            player.takeDamage(1);
        }

        // enemy hit by player
        if (player.isAttacking() && player.getAttackHitbox().overlaps(enemy.getBounds())) {
            enemy.takeDamage(1);
        }

        // collectible pickup
        for (Collectible collectible : collectibles) {
            if (!collectible.isCollected() &&
                player.getBounds().overlaps(collectible.getBounds())) {
                collectible.collect();
                collectedCount++;
                if (collectedCount >= 3) allCollected = true;
            }
        }

        collectiblesLabel.setText("Stones: " + collectedCount + "/3");
        girlStateTime += delta;

        // death check
        if (player.isDead()) {
            game.setScreen(new GameOverScreen(game));
        }

        // portal activation check
        if (allCollected && enemy.isDead() && !portal.isActive()) {
            portal.activate();
        }
        portal.update(delta);

        // win condition — enter the active portal
        if (portal.isActive() && player.getBounds().overlaps(portal.getBounds())) {
            recorder.stopRecording();
            game.database.saveReplay(currentUserId, recorder.getFrames());
            game.setScreen(new WinScreen(game));
        }

        // record complete frame state after all logic is resolved
        recorder.recordFrame(delta, player, enemy, collectibles);

        // draw world
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

        if (!enemy.isDead()) {
            batch.draw(enemy.getCurrentFrame(),
                enemy.getX() - (Enemy.FRAME_WIDTH - 32) / 2f,
                enemy.getY());
        }

        for (Collectible collectible : collectibles) {
            collectible.update(delta);
            if (!collectible.isCollected()) {
                batch.draw(collectible.getCurrentFrame(),
                    collectible.getX(), collectible.getY(),
                    Collectible.SIZE, Collectible.SIZE);
            }
        }

        portal.draw(batch, delta);

        TextureRegion girlFrame = girlAnim.getKeyFrame(girlStateTime, true);
        batch.draw(girlFrame, GIRL_X, GIRL_Y, 39 * 0.95f, 53 * 0.95f);

        batch.end();

        // draw HUD on top
        healthLabel.setText("HP: " + player.getCurrentHealth() + "/ 5");
        hudStage.act(delta);
        hudStage.draw();
    }

    /**
     * Updates the HUD stage viewport on window resize.
     *
     * @param width  new window width in pixels
     * @param height new window height in pixels
     */
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
    }

    @Override public void pause() {}
    @Override public void resume() {}

    /**
     * Stops and disposes the dungeon music when another screen replaces this one.
     */
    @Override
    public void hide() {
        music.stop();
        music.dispose();
    }

    /**
     * Disposes of all assets including batch, map, renderer, HUD, skin,
     * collectibles and music to free GPU and CPU memory.
     */
    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        hudStage.dispose();
        skin.dispose();
        for (Collectible collectible : collectibles) {
            collectible.dispose();
        }
        if (attackTexture != null) attackTexture.dispose();
        if (music != null) {
            music.stop();
            music.dispose();
        }
    }
}
