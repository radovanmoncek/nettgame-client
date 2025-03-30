package cz.radovanmoncek.test;

import cz.radovanmoncek.modules.games.handlers.TestingGameServerChannelHandler;
import cz.radovanmoncek.modules.games.models.RequestFlatBuffersSerializable;
import cz.radovanmoncek.ship.bootstrap.NettgameClientBootstrap;
import cz.radovanmoncek.ship.builders.NettgameClientBootstrapBuilder;
import cz.radovanmoncek.ship.directors.NettgameClientBootstrapDirector;
import cz.radovanmoncek.ship.tables.GameStatus;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//todo: use position attributes in ALL tests !!!! !!!!
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NettgameClientTest {
    private static final int speed = 2;
    private static NettgameClientBootstrap player1;
    private static TestingGameServerChannelHandler gameStateTestingGameServerChannelHandler1;

    @BeforeAll
    static void setup() {

        gameStateTestingGameServerChannelHandler1 = new TestingGameServerChannelHandler(true);
        player1 = new NettgameClientBootstrapDirector(new NettgameClientBootstrapBuilder())
                .makeDefaultGameClientBootstrapBuilder()
                .buildChannelHandler(gameStateTestingGameServerChannelHandler1)
                .build();
    }

    @BeforeAll
    static void runTest() throws InterruptedException {

        player1.run();
        gameStateTestingGameServerChannelHandler1.awaitInitialization();
    }

    @Test
    @Order(0)
    void nicknameOver8CharactersTest() throws InterruptedException {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.START_SESSION)
                .withName("VeryLongNicknameThatIsOverEightCharacters")
        );

        var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());
    }

    @Test
    @Order(1)
    void emptyPlayerNameTest() throws InterruptedException {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable().withGameStatus(GameStatus.START_SESSION));

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());
    }

    @Test
    @Order(2)
    void sessionStartTest() throws InterruptedException {

        final var sessionRequestProtocolDataUnit = new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.START_SESSION)
                .withName("Test");

        gameStateTestingGameServerChannelHandler1.unicast(sessionRequestProtocolDataUnit);

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals("Test", gameState.players(0).name());
        assertEquals(GameStatus.START_SESSION, gameState.gameMetadata().gameStatus());
        assertEquals(400, gameState.players(0).position().x());
        assertEquals(300, gameState.players(0).position().y());
        assertEquals(0, gameState.players(0).position().rotation());
    }

    @Test
    @Order(3)
    void sessionValidStateWithinBoundsTest() throws InterruptedException {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{400 + speed, 300, 90}) //todo: cant move diagonally !!!! !!!!
        );

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.gameMetadata());

        assertEquals(GameStatus.STATE_CHANGE, gameState.gameMetadata().gameStatus());

        assertNotNull(gameState.players(0));

        assertEquals(400 + speed, gameState.players(0).position().x());
        assertEquals(300, gameState.players(0).position().y());
        assertEquals(90, gameState.players(0).position().rotation());
    }

    @Test
    @Order(4)
    void sessionInvalidStateOverBoundsTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{801, 601, 15}));

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());
    }

    @Test
    @Order(5)
    void sessionInvalidStateUnderBoundsTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{-1, -1, -15}));

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());
    }

    @Test
    @Order(6)
    void sessionValidStateMoveDeltaTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{400 + speed, 300 + speed, 180})
        );

        var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.players(0));

        assertEquals(GameStatus.STATE_CHANGE, gameState.gameMetadata().gameStatus());
        assertEquals(400 + speed, gameState.players(0).position().x());
        assertEquals(300 + speed, gameState.players(0).position().y());
        assertEquals(180, gameState.players(0).position().rotation());

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{400 + speed, 300 + speed, 270})
        );

        gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);
        assertNotNull(gameState.players(0));

        assertEquals(GameStatus.STATE_CHANGE, gameState.gameMetadata().gameStatus());
        assertEquals(400 + speed, gameState.players(0).position().x());
        assertEquals(300 + speed, gameState.players(0).position().y());
        assertEquals(270, gameState.players(0).position().rotation());
    }

    @Test
    @Order(7)
    void sessionInvalidStateMoveDeltaTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{2, 6, 45})
        );

        var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable().withGameStatus(GameStatus.STATE_CHANGE).withPosition(new int[]{4, 4, 45}));

        gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.INVALID_STATE, gameState.gameMetadata().gameStatus());
    }

    @Test
    @Order(8)
    void sessionEndTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable().withGameStatus(GameStatus.STOP_SESSION));

        final var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.STOP_SESSION, gameState.gameMetadata().gameStatus());
    }

    private static int
            x1 = 400,
            y1 = 300;

    @Test
    @Order(9)
    void startAnotherSessionTest() throws Exception {

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable().withGameStatus(GameStatus.START_SESSION).withName("Test"));

        var gameState = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameState);

        assertEquals(GameStatus.START_SESSION, gameState.gameMetadata().gameStatus());

        // todo: test join ???? ????
    }

    @RepeatedTest(400)
    @Order(10)
    void onePlayerMovementStressTest() throws Exception {

        if (x1 >= 792)
            return;

        gameStateTestingGameServerChannelHandler1.unicast(new RequestFlatBuffersSerializable()
                .withGameStatus(GameStatus.STATE_CHANGE)
                .withPosition(new int[]{x1 + speed, y1, 90}));

        var gameStateFirstPlayer = gameStateTestingGameServerChannelHandler1.poll();

        assertNotNull(gameStateFirstPlayer);

        assertEquals(GameStatus.STATE_CHANGE, gameStateFirstPlayer.gameMetadata().gameStatus());

        assertEquals(x1 + speed, gameStateFirstPlayer.players(0).position().x());
        assertEquals(y1, gameStateFirstPlayer.players(0).position().y());
        assertEquals(90, gameStateFirstPlayer.players(0).position().rotation());

        x1 = (int) gameStateFirstPlayer.players(0).position().x();
        y1 = (int) gameStateFirstPlayer.players(0).position().y();
    }

    @AfterAll
    static void tearDown() {

        gameStateTestingGameServerChannelHandler1.disconnect();
    }
}
