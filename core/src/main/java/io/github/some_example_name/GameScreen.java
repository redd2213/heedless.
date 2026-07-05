package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

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
    private Label pauseLabel;
    private Label objectiveLabel;
    private Skin skin;
    private FrameRecorder recorder;
    private int currentUserId; //hardcoded, will wire to login later (forgot about this and debugged the replay system for 30 minutes :D )
    private boolean paused = false;
    private Collectible[] collectibles;
    private int collectedCount = 0;
    private boolean allCollected = false;
    private Label collectiblesLabel;
    private Texture attackTexture;
    private Label attackLabel;
    private Portal portal;

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
        float zoomFactor = 1.4f; // camera zoom because the pixel art is relatively small compared to global res
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth() / zoomFactor, Gdx.graphics.getHeight() / zoomFactor);

        //map import and init
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");

        // enemy init
        enemy = new Enemy(16, 256, 16, 496);

        // collectibles init
        collectibles = new Collectible[] {
            //had to improvise inital coordinates because of sprite size change from 16x16 to 32x32
            new Collectible(745, 176), //COORD 1
            new Collectible(105, 432), //COORD 2
            new Collectible(460, 400)  //COORD 3
        };

        //portal init
        portal = new Portal(656,360);

        //hud camera setup
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //hud init
        //health hud
        hudStage = new Stage(new ScreenViewport());
        healthLabel = new Label("HP: " + player.getCurrentHealth(), skin, "title");
        healthLabel.setFontScale(0.5f);
        healthLabel.setPosition(20, Gdx.graphics.getHeight() -100);
        hudStage.addActor(healthLabel);

        //collectibles hud
        collectiblesLabel = new Label("Stones : 0/3", skin, "title");
        collectiblesLabel.setFontScale(0.5f);
        collectiblesLabel.setPosition(20, Gdx.graphics.getHeight() -160);
        hudStage.addActor(collectiblesLabel);

        //objective hud
        objectiveLabel = new Label("Objective: Collect all the Energy Stones and slay all the monsters to unlock the Portal to the next room!", skin);
        objectiveLabel.setFontScale(1f);
        objectiveLabel.setPosition(300, Gdx.graphics.getHeight() -1050);
        hudStage.addActor(objectiveLabel);

        //tutorial hud
        Texture attackTexture = new Texture(Gdx.files.internal("Assets/SPRITES/player/Punch/sprites/f-02.png"));
        Image attackImage = new Image(attackTexture);

        Table tutorialTable = new Table();
        tutorialTable.setFillParent(true);
        tutorialTable.bottom().padBottom(150);
        tutorialTable.add(attackImage).size(attackTexture.getWidth() * 2f, attackTexture.getHeight() * 2f);
        Label attackLabel = new Label("= SPACE", skin);
        tutorialTable.add(attackLabel).padLeft(10).padTop(50);
        tutorialTable.getColor().a = 0f;

        tutorialTable.addAction(Actions.sequence(
            Actions.fadeIn(1f),     //1 second to fade in
            Actions.delay(4f),      //wait on screen for 4 seconds
            Actions.fadeOut(1f),    //1 second to fade out
            Actions.removeActor()
        ));

        hudStage.addActor(tutorialTable);

        //pause hud
        Table pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();
        pauseLabel = new Label("PAUSED", skin, "title");
        pauseLabel.setVisible(false);
        pauseTable.add(pauseLabel);
        hudStage.addActor(pauseTable);



        currentUserId = game.currentUserId;
        recorder = new FrameRecorder();
        recorder.startRecording();
    }

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.

        //detect single-frame events before updating player
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && player.isGrounded()) {
            recorder.onJumpPressed();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !player.isAttacking()) {
            recorder.onAttackPressed();
        }

        //pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            pauseLabel.setVisible(paused);
        }

        //skip logic and drawing updates when paused
        if (paused) {
            hudStage.act(delta);
            hudStage.draw();
            return;
        }

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

        //collectible pickup check
        for (Collectible collectible : collectibles) {
            if (!collectible.isCollected() &&
            player.getBounds().overlaps(collectible.getBounds())) {
                collectible.collect();
                collectedCount++;
                if (collectedCount >= 3) {
                    allCollected = true;
                }
            }
        }

        //collectible HUD update
        collectiblesLabel.setText("Stones: " + collectedCount + "/3");

        // death check
        if (player.isDead()) {
            game.setScreen(new GameOverScreen(game));
        }

        //objective check
        if (allCollected && enemy.isDead() && !portal.isActive()) {
            portal.activate();
        }
        portal.update(delta);

        // win check
        if (portal.isActive() && player.getBounds().overlaps(portal.getBounds())) {
            recorder.stopRecording();
            game.database.saveReplay(currentUserId, recorder.getFrames());
            game.setScreen(new WinScreen(game));
        }

        //record complete frame state after update
        recorder.recordFrame(delta, player, enemy, collectibles);

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

        //draw uncollected collectibles
        for (Collectible collectible : collectibles) {
            collectible.update(delta);
            if (!collectible.isCollected()) {
                batch.draw(collectible.getCurrentFrame(),
                    collectible.getX(), collectible.getY(),
                    Collectible.SIZE, Collectible.SIZE // force render to 16x16
                );
            }
        }

        //draw portal
        portal.draw(batch, delta);

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
        for (Collectible collectible : collectibles) {
            collectible.dispose();
        }
        if (attackTexture != null) attackTexture.dispose();
    }
}
