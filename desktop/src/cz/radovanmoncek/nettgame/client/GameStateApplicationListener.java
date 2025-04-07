package cz.radovanmoncek.nettgame.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.net.Socket;
import com.badlogic.gdx.net.SocketHints;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import cz.radovanmoncek.nettgame.tables.GameState;
import cz.radovanmoncek.nettgame.tables.GameStatus;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

//todo: client side prediction time delta time topic in Thesis ???? ????
//https://stackoverflow.com/questions/6307648/change-global-setting-for-logger-instances

/**
 * An example client for the containerized nettgame server that
 * handles all the underlying logic and maintains a connection to the Nettgame sever.
 *
 * @author Radovan Monček
 * @apiNote This class utilizes the <a href=https://refactoring.guru>Singleton Design Pattern</a>, since it is bound to a Socket.
 * There can exist, at most, one instance of this class per runtime.
 * @since 1.0
 */
public class GameStateApplicationListener implements ApplicationListener {

    /**
     * Singleton instance
     */
    private static GameStateApplicationListener instance;

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
    private final AtomicReference<Socket> clientSocket = new AtomicReference<>();
    private final InetAddress gameServerAddress;
    /**
     * The number of seconds between reconnect attempts.
     */
    private final int reconnectDelayMilliseconds;
    private final int gameServerPort;

    private Viewport viewport;
    private SpriteBatch batch,
                        noViewportBatch;
    private float deltaTime;

    /**
     * Source: <a href = https://gamefromscratch.com/libgdx-tutorial-10-basic-networking/>gfs</a>
     */
    private GameStateApplicationListener() {

        gameServerPort = 4321;
        gameServerAddress = InetAddress.getLoopbackAddress();
        gameStates = new ConcurrentLinkedQueue<>();
        disposables = new LinkedList<>();
        clientState = new AtomicReference<>();
        mainMenuClientState = new MainMenuClientState(this::unicast);
        gameSessionRunningClientState = new GameSessionRunningClientState(this::unicast);
        reconnectDelayMilliseconds = 1000;
    }

    public void create0(){

        final var socketHints = new SocketHints();

        socketHints.connectTimeout = 5000;
        socketHints.keepAlive = true;
        socketHints.tcpNoDelay = true;

        clientSocket.set(Gdx
                .net
                .newClientSocket(Net.Protocol.TCP, gameServerAddress.getHostName(), gameServerPort, socketHints)
        );
        disposables.add(clientSocket.get());

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
                            //Thank you: https://kpavlov.me/blog/implementing-automatic-reconnection-for-netty-client/

                            TimeUnit.MILLISECONDS.sleep(1000);

                            final var start = System.nanoTime();

                            while (!gameServerAddress.isReachable(1000) || !clientSocket.get().isConnected()) {

                                clientState.set(mainMenuClientState);
                                clientSocket
                                        .get()
                                        .dispose();
                                clientSocket.set(Gdx
                                        .net
                                        .newClientSocket(Net.Protocol.TCP, gameServerAddress.getHostName(), gameServerPort, socketHints));
                                disposables.add(clientSocket.get());

                                Gdx
                                        .app
                                        .log("GameStateApplicationListener", "Got unreachable status, ping will not be reported");

                                TimeUnit.MILLISECONDS.sleep(reconnectDelayMilliseconds);
                            }

                            final var end = System.nanoTime() - start;

                            Gdx.app.debug("ping", end + " ms");

                            ping.set(end / (1000 * 1000));

                            if (clientState.get() == null) {

                                continue;
                            }

                            clientState
                                    .get()
                                    .pingUpdate(ping.get());
                        } catch (final InterruptedException | IOException exception) {

                            Gdx.app.error("GameServerChannelHandler", "Interrupted", exception);
                        }
                    }
                })
                .start();
    }

    private void unicast(final ByteBuffer request) {

        try {

            final var byteBuffer = ByteBuffer
                    .allocate(Long.BYTES + Byte.BYTES + request.remaining())
                    .putLong(Byte.BYTES + request.remaining())
                    .put((byte) 'g')
                    .put(request);

            clientSocket
                    .get()
                    .getOutputStream()
                    .write(byteBuffer.array());
        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    private void startBlockingGameStateDispatcher() {

        try {

            while (!ended.get()) {

                if (clientState.get() == null) {

                    continue;
                }

                final var length = ByteBuffer.wrap(clientSocket.get().getInputStream().readNBytes(8)).getLong();
                final var body = ByteBuffer.wrap(clientSocket.get().getInputStream().readNBytes((int) length));

                if (body.get() != 'G')
                    continue;

                final var gameState = GameState.getRootAsGameState(body);

                gameStates.offer(gameState);

                Gdx
                        .app
                        .log("Game state received", "processing game state " + gameState.gameMetadata().gameStatus());

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

                    case GameStatus.INVALID_STATE -> Gdx
                            .app
                            .debug("Game state received", "invalid game state");
                }

                clientState
                        .get()
                        .processGameState(gameStates.poll());

                Gdx
                        .app
                        .log("Session response", "Session response received from the server " + gameState.gameMetadata().gameStatus());

                //todo: proper non-naive implementation (client-side prediction)
            }
        } catch (Exception e) {

            Gdx
                    .app
                    .error("GameStateApplicationListener", e.getMessage(), e);
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

        create0();
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

        ended.set(true);

        try {

            TimeUnit.MILLISECONDS.sleep(30);
        } catch (InterruptedException e) {

            Gdx
                    .app
                    .error("GameStateApplicationListener", "Interrupted", e);
        }

        disposables.forEach(Disposable::dispose);
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

    //https://stackoverflow.com/questions/6307648/change-global-setting-for-logger-instances

    public static GameStateApplicationListener returnNewInstance() {

        return Objects.requireNonNullElse(instance, instance = new GameStateApplicationListener());
    }
}
