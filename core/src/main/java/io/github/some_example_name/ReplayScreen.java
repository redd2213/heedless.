package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
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
    private Portal portal;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private TiledMapTileLayer layer;
    private Music music;

    //girl animation
    private Animation<TextureRegion> girlAnim;
    private float girlStateTime = 0f;
    private static final float GIRL_X = 590f;
    private static final float GIRL_Y = 367f;

    //hud
    private Stage hudStage;
    private Skin skin;
    private Label healthLabel;
    private Label collectiblesLabel;

    //replay data
    private Array<FrameState> frames;
    private int frameIndex = 0;

    public ReplayScreen(Main game, int userId) {
        this.game = game;
        this.frames = game.database.loadReplay(userId);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();

        //player
        player = new Player();

        //enemy
        enemy = new Enemy(16, 256, 16, 496);

        //collectibles
        collectibles = new Collectible[] {
            new Collectible(745, 176),
            new Collectible(105, 432),
            new Collectible(460, 400)
        };

        //portal
        portal = new Portal(656, 360);

        //girl animation
        Texture girlSheet = new Texture(Gdx.files.internal(
            "Assets/SPRITES/Dancing Girl Files/spritesheets/snap.png"));
        TextureRegion[][] girlTmp = TextureRegion.split(girlSheet,
            girlSheet.getWidth() / 8, girlSheet.getHeight());
        Array<TextureRegion> girlFrames = new Array<>();
        for (int i = 0; i < 8; i++) {
            girlFrames.add(girlTmp[0][i]);
        }
        girlAnim = new Animation<>(0.1f, girlFrames);

        //camera
        float zoomFactor = 1.4f;
        camera = new OrthographicCamera();
        camera.setToOrtho(false,
            Gdx.graphics.getWidth() / zoomFactor,
            Gdx.graphics.getHeight() / zoomFactor);

        //map
        map = new TmxMapLoader().load("map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        layer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");

        //hud
        skin = new Skin(Gdx.files.internal("pixthulhu/pixthulhu-ui.json"));
        hudStage = new Stage(new ScreenViewport());

        healthLabel = new Label("HP: 5/ 5", skin, "title");
        healthLabel.setFontScale(0.5f);
        healthLabel.setPosition(20, Gdx.graphics.getHeight() - 100);
        hudStage.addActor(healthLabel);

        collectiblesLabel = new Label("Stones: 0/3", skin, "title");
        collectiblesLabel.setFontScale(0.5f);
        collectiblesLabel.setPosition(20, Gdx.graphics.getHeight() - 160);
        hudStage.addActor(collectiblesLabel);

        //music setup
        music = Gdx.audio.newMusic(Gdx.files.internal("Assets/Music (Crimson Hollow by Andy Martinez on itch.io)/Dungeon(GameScreen).wav"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        //going back to menu
        if (frameIndex >= frames.size) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        FrameState fs = frames.get(frameIndex);
        frameIndex++;

        //state restore
        player.restoreState(fs);

        if (!fs.enemyDead) {
            enemy.restoreState(fs);
        }

        //restore collectibles
        int collectedCount = 0;
        if (fs.stone0) collectibles[0].collect();
        if (fs.stone1) collectibles[1].collect();
        if (fs.stone2) collectibles[2].collect();
        if (collectibles[0].isCollected()) collectedCount++;
        if (collectibles[1].isCollected()) collectedCount++;
        if (collectibles[2].isCollected()) collectedCount++;

        //restore portal
        if (fs.stone0 && fs.stone1 && fs.stone2 && fs.enemyDead) {
            if (!portal.isActive()) portal.activate();
        }
        portal.update(fs.delta);

        //advance girl anim
        girlStateTime += fs.delta;

        //draw
        ScreenUtils.clear(Color.BLACK);
        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        //draw uncollected collectibles
        for (Collectible c : collectibles) {
            if (!c.isCollected()) {
                c.update(fs.delta);
                batch.draw(c.getCurrentFrame(),
                    c.getX(), c.getY(),
                    Collectible.SIZE, Collectible.SIZE);
            }
        }

        //draw portal
        portal.draw(batch, fs.delta);

        //draw girl
        TextureRegion girlFrame = girlAnim.getKeyFrame(girlStateTime, true);
        batch.draw(girlFrame, GIRL_X, GIRL_Y, 39 * 0.95f, 53 * 0.95f);

        //draw player
        batch.draw(player.getCurrentFrameForReplay(),
            player.getX() - (Player.FRAME_WIDTH - 32) / 2f,
            player.getY());

        //draw enemy if alive
        if (!fs.enemyDead) {
            enemy.advanceAnimation(fs.delta);
            batch.draw(enemy.getCurrentFrame(),
                enemy.getX() - (Enemy.FRAME_WIDTH - 32) / 2f,
                enemy.getY());
        }

        batch.end();

        //hud
        healthLabel.setText("HP: " + fs.playerHealth + "/ 5");
        collectiblesLabel.setText("Stones: " + collectedCount + "/3");
        hudStage.act(fs.delta);
        hudStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        float zoomFactor = 1.4f;
        camera.setToOrtho(false, width / zoomFactor, height / zoomFactor);
        hudStage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}

    @Override public void resume() {}

    @Override public void hide() {
        music.stop();
        music.dispose();
    }

    @Override
    public void dispose() {
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        hudStage.dispose();
        skin.dispose();
        for (Collectible c : collectibles) c.dispose();
        if (music != null) {
            music.stop();
            music.dispose();
        }
    }
}
