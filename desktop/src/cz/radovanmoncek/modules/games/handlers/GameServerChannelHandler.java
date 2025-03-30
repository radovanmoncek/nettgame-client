package cz.radovanmoncek.modules.games.handlers;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import cz.radovanmoncek.modules.games.states.GameSessionRunningClientState;
import cz.radovanmoncek.modules.games.states.MainMenuClientState;
import cz.radovanmoncek.ship.parents.states.ClientState;
import cz.radovanmoncek.ship.parents.handlers.ServerChannelHandler;
import cz.radovanmoncek.ship.tables.GameState;
import cz.radovanmoncek.ship.tables.GameStatus;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//todo: client side prediction time delta time topic in Thesis ???? ????
//https://stackoverflow.com/questions/6307648/change-global-setting-for-logger-instances
public class GameServerChannelHandler extends ServerChannelHandler<GameState> implements ApplicationListener {
    private float deltaTime;
    private final ConcurrentLinkedQueue<GameState> gameStates;
    private final LinkedList<Disposable> disposables;
    /**
     * State (state machine) design pattern
     */
    private final AtomicReference<ClientState> clientState;
    private final ClientState mainMenuClientState,
    gameSessionRunningClientState;
    private final AtomicBoolean ended = new AtomicBoolean(false);
    private final AtomicLong ping = new AtomicLong(0);

    private Viewport viewport;
    private SpriteBatch
            batch,
            noViewportBatch;

    public GameServerChannelHandler(final boolean windowed) {

        gameStates = new ConcurrentLinkedQueue<>();
        disposables = new LinkedList<>();
        clientState = new AtomicReference<>();
        mainMenuClientState = new MainMenuClientState(this::unicast);
        gameSessionRunningClientState = new GameSessionRunningClientState(this::unicast);

        Executors
                .defaultThreadFactory()
                .newThread(() -> {

                    startBlockingGdx(windowed);

                    ended.set(true);
                })
                .start();

        Executors
                .defaultThreadFactory()
                .newThread(this::startBlockingGameStateDispatcher)
                .start();

        //https://www.youtube.com/watch?v=TXh8mMuEkQE
        Executors
                .defaultThreadFactory()
                .newThread(() -> {

                    while (!ended.get()) {

                        try {

                            TimeUnit.MILLISECONDS.sleep(1000);

                            final var start = System.nanoTime();

                            checkIfServerAddressIsReachable(1000);

                            final var end = System.nanoTime() - start;

                            Gdx.app.debug("ping", end + " ms");

                            ping.set(end / (1000 * 1000));

                            if (clientState.get() == null) {

                                continue;
                            }

                            clientState
                                    .get()
                                    .pingUpdate(ping.get());
                        } catch (InterruptedException | IOException exception) {

                            Gdx.app.error("GameServerChannelHandler", "Interrupted", exception);
                        }
                    }
                })
                .start();
    }

    private void startBlockingGdx(final boolean windowed) {

        final var config = new Lwjgl3ApplicationConfiguration();

        config.setForegroundFPS(60);
        config.setTitle("example nettgame client");
        config.setWindowedMode(800, 600);

        if (!windowed)
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());

        new Lwjgl3Application(this, config);
    }

    private void startBlockingGameStateDispatcher(){

        while(!ended.get()) {

            if (clientState.get() == null || gameStates.isEmpty() || clientState.get() instanceof MainMenuClientState) {

                continue;
            }

            Gdx.app.debug("Game state received", "processing game state " + gameStates.peek());

            clientState
                    .get()
                    .processGameState(gameStates.poll());
        }
    }

    @Override
    public void create() {

        viewport = new FitViewport(8f, 6f);
        batch = new SpriteBatch();
        noViewportBatch = new SpriteBatch();
        disposables.add(batch);
        disposables.add(noViewportBatch);
        mainMenuClientState.initialize(disposables);
        gameSessionRunningClientState.initialize(disposables);
        clientState.set(mainMenuClientState);
        clientState
                .get()
                .registered();
    }

    @Override
    public void render() {

        deltaTime += Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        clientState
                .get()
                .render(viewport, batch, deltaTime);
        batch.end();
        noViewportBatch.begin();
        clientState
                .get()
                .noViewportRender(viewport, noViewportBatch, deltaTime);
        noViewportBatch.end();
    }

    @Override
    public void dispose() {

        disposables.forEach(Disposable::dispose);

        disconnect();

        ended.set(true);
    }

    @Override
    public void resize(int width, int height) {

        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext channelHandlerContext, final GameState gameState) {

        switch (gameState.gameMetadata().gameStatus()) {

            case GameStatus.START_SESSION, GameStatus.JOIN_SESSION -> {

                clientState.set(gameSessionRunningClientState);
                clientState
                        .get()
                        .registered();
            }

            case GameStatus.STOP_SESSION -> {

                gameStates.clear();
                clientState.set(mainMenuClientState);
                clientState
                        .get()
                        .registered();
            }

            case GameStatus.INVALID_STATE -> Gdx.app.debug("Game state received", "invalid game state");
        }

        Gdx.app.log("Session response", "Session response received from the server " + gameState.gameMetadata().gameStatus());

        //todo: proper non-naive implementation (client-side prediction)

        gameStates.offer(gameState);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        super.exceptionCaught(ctx, cause);
    }
}
