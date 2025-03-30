package cz.radovanmoncek.ship.directors;

import cz.radovanmoncek.modules.games.codecs.GameStateFlatBufferDecoder;
import cz.radovanmoncek.modules.games.codecs.GameStateRequestFlatBufferEncoder;
import cz.radovanmoncek.ship.builders.NettgameClientBootstrapBuilder;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class NettgameClientBootstrapDirector {
    private final NettgameClientBootstrapBuilder builder;

    public NettgameClientBootstrapDirector(NettgameClientBootstrapBuilder builder) {

        this.builder = builder;
    }

    public NettgameClientBootstrapBuilder makeDefaultGameClientBootstrapBuilder(){

        return builder
                .buildServerAddress(InetAddress.getLoopbackAddress())
                .buildPort(4321)
                .buildShutdownOnDisconnect(true)
                .buildLogLevel(LogLevel.INFO)
                .buildChannelHandler(new GameStateFlatBufferDecoder())
                .buildChannelHandler(new GameStateRequestFlatBufferEncoder())
                .buildReconnectAttempts(10)
                .buildReconnectDelay(4)
                .buildShutdownTimeout(4);
    }
}
