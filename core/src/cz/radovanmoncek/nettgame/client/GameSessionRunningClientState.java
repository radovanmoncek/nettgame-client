package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.nettgame.tables.*;
import cz.radovanmoncek.nettgame.tables.Character;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameSessionRunningClientState implements ClientState {

    private static final Logger logger = Logger.getLogger(GameSessionRunningClientState.class.getName());
    private static final float speed = 2.0f;

    private final Consumer<ByteBuffer> unicast;
    private final AtomicReference<String> gameCode = new AtomicReference<>("");
    private final AtomicLong
            gameLength,
            lastRequestDelta;
    private final AtomicBoolean
            stateRequested = new AtomicBoolean(false),
            joined = new AtomicBoolean(false),
            startReceived = new AtomicBoolean(false);

    private Sprite rock,
            rock2;
    private Player lastPlayerState,
            lastPlayerState2;
    private Texture background;
    private BitmapFont font;
    private Animation<Sprite>
            animationStatePlayer1,
            idleAnimationRed,
            walkingAnimationRed,
            animationStatePlayer2,
            idleAnimationBlue,
            walkingAnimationBlue;
    private int
            oldFlipAnglePlayer1 = 90,
            oldFlipAnglePlayer2 = 90;

    public GameSessionRunningClientState(final Consumer<ByteBuffer> unicast) {

        this.unicast = unicast;

        gameLength = new AtomicLong(0);
        lastRequestDelta = new AtomicLong(0);
    }

    @Override
    public void pingUpdate(long ping) {

        lastRequestDelta.set(ping);
    }

    @Override
    public void processGameState(GameState gameState) {

        if (Objects.isNull(gameState)) {

            return;
        }

        if (gameState.gameMetadata().gameStatus() == GameStatus.START_SESSION || gameState.gameMetadata().gameStatus() == GameStatus.JOIN_SESSION)
            startReceived.set(true);

        if (!Objects.requireNonNullElse(gameState.gameMetadata().gameCode(), "").isBlank() && !gameCode.get().equals(gameState.gameMetadata().gameCode()))
            gameCode.set(
                    gameState
                            .gameMetadata()
                            .gameCode()
            );

        Optional
                .of(gameState
                        .gameMetadata()
                        .length())
                .ifPresent(gameLength::set);

        if (gameState.playersLength() >= 1 && Objects.nonNull(gameState.players(0)) && Objects.nonNull(gameState.players(0).position())) {

            final var player1 = gameState.players(0);

            if (lastPlayerState == null) {

                lastPlayerState = player1;
            }

            final var translationX = (player1.position().x() - lastPlayerState.position().x()) / 100f;
            final var translationY = (player1.position().y() - lastPlayerState.position().y()) / 100f;

            logger.log(Level.INFO, "Rendering player state x: {0} y: {1} rotationAngle: {2}", new Object[]{translationX, translationY, player1.position().rotation()});

            if ((player1.position().rotation() == 90 || player1.position().rotation() == 270) && oldFlipAnglePlayer1 != player1.position().rotation()) {

                oldFlipAnglePlayer1 = player1.position().rotation();

                Arrays.stream(walkingAnimationRed.getKeyFrames()).forEach(keyFrame -> keyFrame.flip(true, false));
                Arrays.stream(idleAnimationRed.getKeyFrames()).forEach(keyFrame -> keyFrame.flip(true, false));
            }

            Arrays.stream(walkingAnimationRed.getKeyFrames()).forEach(keyFrame -> keyFrame.translate(translationX, translationY));
            Arrays.stream(idleAnimationRed.getKeyFrames()).forEach(keyFrame -> keyFrame.translate(translationX, translationY));

            lastPlayerState = player1;
        }

        if (gameState.playersLength() == 2 && Objects.nonNull(gameState.players(1)) && Objects.nonNull(gameState.players(1).position())) {

            if (lastPlayerState == null)
                lastPlayerState = gameState.players(0);

            final var player2 = gameState.players(1);

            if (lastPlayerState2 == null) {

                lastPlayerState2 = player2;
            }

            joined.set(true);

            final var translationX = (player2.position().x() - lastPlayerState2.position().x()) / 100f;
            final var translationY = (player2.position().y() - lastPlayerState2.position().y()) / 100f;

            logger.log(Level.INFO, "Rendering player state x: {0} y: {1} rotationAngle: {2}", new Object[]{translationX, translationY, player2.position().rotation()});

            if ((player2.position().rotation() == 90 || player2.position().rotation() == 270) && oldFlipAnglePlayer2 != player2.position().rotation()) {

                oldFlipAnglePlayer2 = player2.position().rotation();

                Arrays.stream(walkingAnimationBlue.getKeyFrames()).forEach(keyFrame -> keyFrame.flip(true, false));
                Arrays.stream(idleAnimationBlue.getKeyFrames()).forEach(keyFrame -> keyFrame.flip(true, false));
            }

            Arrays
                    .stream(walkingAnimationBlue.getKeyFrames())
                    .forEach(keyFrame -> keyFrame.translate(translationX, translationY));
            Arrays
                    .stream(idleAnimationBlue.getKeyFrames())
                    .forEach(keyFrame -> keyFrame.translate(translationX, translationY));

            lastPlayerState2 = player2;
            stateRequested.set(false);

            return;
        }

        joined.set(false);
        stateRequested.set(false);
    }

    @Override
    public void noViewportRender(Viewport viewport, SpriteBatch batch, float deltaTime) {

        //https://www.bilibili.com/video/BV1Ta4y147gs?uid=425631546134793134376773&spm_id_from=333.788.videopod.episodes&p=13

        if (Gdx.input.isKeyPressed(Input.Keys.TAB)) {

            font.draw(batch, "Game code to join: " + gameCode.get(), Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        }

        font.draw(batch, gameLength.get() / 1000L / 60L + ":" + gameLength.get() / 1000L % 60L, 20f, 20f);
        font.draw(batch, "ping: " + lastRequestDelta + " ms", 80f, 20f); //todo: serverHandler should calculate

        gameLength.set(gameLength.addAndGet(17));
    }

    @Override
    public void render(Viewport viewport, SpriteBatch batch, float deltaTime) {

        processInputs();

        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        rock.draw(batch);
        rock2.draw(batch);
        animationStatePlayer1.getKeyFrame(deltaTime, true).draw(batch);

        if (joined.get())
            animationStatePlayer2.getKeyFrame(deltaTime, true).draw(batch);
    }

    private void processInputs() {

        if (!startReceived.get() || stateRequested.get())
            return;

        ByteBuffer serializable = null;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {

            serializable = (serializeStateRequest(new int[]{(int) lastPlayerState.position().x(), Math.round(lastPlayerState.position().y() + speed), 0}, Character.BLUE));
        } else if (Gdx.input.isKeyPressed(Input.Keys.A)) {

            serializable = (serializeStateRequest(new int[]{Math.round(lastPlayerState.position().x() - speed), (int) lastPlayerState.position().y(), 270}, Character.BLUE));
        } else if (Gdx.input.isKeyPressed(Input.Keys.S)) {

            serializable = (serializeStateRequest(new int[]{(int) lastPlayerState.position().x(), Math.round(lastPlayerState.position().y() - speed), 180}, Character.BLUE));
        } else if (Gdx.input.isKeyPressed(Input.Keys.D)) {

            serializable = (serializeStateRequest(new int[]{Math.round(lastPlayerState.position().x() + speed), (int) lastPlayerState.position().y(), 90}, Character.BLUE));
        }

        if (serializable == null)
            return;

        animationStatePlayer1 = walkingAnimationRed;

        unicast.accept(serializable);
        stateRequested.set(true);
    }

    @Override
    public void registered() {

        Gdx.input.setInputProcessor(new InputAdapter() {

            @Override
            public boolean keyDown(int keycode) {

                if (keycode == Input.Keys.ESCAPE) {

                    unicast.accept(serializeStopRequest());

                    return true;
                }

                return false;
            }

            @Override
            public boolean keyUp(int keycode) {

                animationStatePlayer1 = idleAnimationRed;

                return false;
            }
        });
    }

    //todo: optimisation to PR
    public void initialize(final LinkedList<Disposable> disposables) {

        //https://rgsdev.itch.io/free-cc0-modular-animated-vector-characters-2d
        final var generator = new FreeTypeFontGenerator(Gdx.files.internal("font/PressStart2P-Regular.ttf"));
        final var parameter = new FreeTypeFontGenerator
                .FreeTypeFontParameter();
        final var rockTexture = new Texture("Free 2D Animated Vector Game Character Sprites/Environment/rock1.png");
        final var rockTexture2 = new Texture("Free 2D Animated Vector Game Character Sprites/Environment/rock2.png");

        parameter.size = 8;

        idleAnimationRed = new Animation<>(1f / 6f,
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_0.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_1.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_2.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_3.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_4.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/idle_5.png"))
        );
        walkingAnimationRed = new Animation<>(1f / 8f,
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_0.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_1.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_2.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_3.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_4.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_5.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_6.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 4/with hands/walk_7.png"))
        );
        animationStatePlayer1 = idleAnimationRed;
        idleAnimationBlue = new Animation<>(1f / 6f,
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_0.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_1.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_2.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_3.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_4.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/idle_5.png"))
        );
        walkingAnimationBlue = new Animation<>(1f / 8f,
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_0.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_1.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_2.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_3.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_4.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_5.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_6.png")),
                new Sprite(new Texture("Free 2D Animated Vector Game Character Sprites/Full body animated characters/Char 1/with hands/walk_7.png"))
        );
        animationStatePlayer2 = idleAnimationBlue;
        background = new Texture("Free 2D Animated Vector Game Character Sprites/Environment/ground_white.png");
        rock = new Sprite(rockTexture);
        rock2 = new Sprite(rockTexture2);
        font = generator.generateFont(parameter);

        font.setColor(Color.BLACK);
        //https://stackoverflow.com/questions/33633395/how-set-libgdx-bitmap-font-size
        //https://stackoverflow.com/questions/12466385/how-can-i-draw-text-using-libgdx-java
        //https://stackoverflow.com/questions/34046216/font-and-viewport-libgdx

        rock.setSize(1, 1);
        rock.translate(4f, 3f);

        rock2.setSize(1, 1);
        rock2.translate(2f, 1.32f);

        for (final var keyFrame : idleAnimationRed.getKeyFrames()) {

            keyFrame.setSize(1, 1);
            keyFrame.setOrigin(4, 3);
            keyFrame.translate(4f, 3f);
            disposables.add(keyFrame.getTexture());
        }

        for (final var keyFrame : walkingAnimationRed.getKeyFrames()) {

            keyFrame.setSize(1, 1);
            keyFrame.setOrigin(4, 3);
            keyFrame.translate(4f, 3f);
            disposables.add(keyFrame.getTexture());
        }

        for (final var keyFrame : idleAnimationBlue.getKeyFrames()) {

            keyFrame.setSize(1, 1);
            keyFrame.setOrigin(4, 3);
            keyFrame.translate(4f, 3f);
            disposables.add(keyFrame.getTexture());
        }

        for (final var keyFrame : walkingAnimationBlue.getKeyFrames()) {

            keyFrame.setSize(1, 1);
            keyFrame.setOrigin(4, 3);
            keyFrame.translate(4f, 3f);
            disposables.add(keyFrame.getTexture());
        }

        disposables.add(rockTexture);
        disposables.add(rockTexture2);
        disposables.add(font);
        disposables.add(background);
        disposables.add(generator);
    }

    private ByteBuffer serializeStateRequest(int[] position, byte character) {

        final var builder = new FlatBufferBuilder(1024);
        final var name = builder.createString("");
        final var gameCode = builder.createString(this.gameCode.get());

        Player.startPlayer(builder);
        Player.addCharacter(builder, character);

        if (Objects.nonNull(position)) {
            Player.addPosition(builder, Position.createPosition(builder, position[0], position[1], position[2]));
        }

        Player.addName(builder, name);

        final var player = Player.endPlayer(builder);

        Request.startRequest(builder);
        Request.addGameStatus(builder, GameStatus.STATE_CHANGE);
        Request.addPlayer(builder, player);
        Request.addGameCode(builder, gameCode);

        final var gameStateRequest = Request.endRequest(builder);

        builder.finish(gameStateRequest);

        return ByteBuffer.wrap(builder.sizedByteArray());
    }

    private ByteBuffer serializeStopRequest() {

        final var builder = new FlatBufferBuilder(1024);
        final var name = builder.createString("");
        final var gameCode = builder.createString(this.gameCode.get());

        Player.startPlayer(builder);

        Player.addName(builder, name);

        final var player = Player.endPlayer(builder);

        Request.startRequest(builder);
        Request.addGameStatus(builder, GameStatus.STOP_SESSION);
        Request.addPlayer(builder, player);
        Request.addGameCode(builder, gameCode);

        final var gameStateRequest = Request.endRequest(builder);

        builder.finish(gameStateRequest);

        return ByteBuffer.wrap(builder.sizedByteArray());
    }
}
