package cz.radovanmoncek.ship.builders;

import cz.radovanmoncek.ship.bootstrap.NettgameClientBootstrap;
import cz.radovanmoncek.ship.parents.builders.Builder;
import io.netty.channel.ChannelHandler;
import io.netty.handler.logging.LogLevel;

import java.net.InetAddress;

public class NettgameClientBootstrapBuilder implements Builder<NettgameClientBootstrap> {
    private NettgameClientBootstrap result;

    public NettgameClientBootstrapBuilder() {

        result = NettgameClientBootstrap.returnNewInstance();
    }

    @Override
    public NettgameClientBootstrap build() {

        return result;
    }

    @Override
    public Builder<NettgameClientBootstrap> reset() {

        result = null;

        return this;
    }

    public NettgameClientBootstrapBuilder buildShutdownOnDisconnect(final boolean shutdownOnDisconnect) {

        result.setShutdownOnDisconnect(shutdownOnDisconnect);

        return this;
    }

    public NettgameClientBootstrapBuilder buildPort(int port){

        result.setGameServerPort(port);

        return this;
    }

    public NettgameClientBootstrapBuilder buildServerAddress(InetAddress address) {

        result.setGameServerAddress(address);

        return this;
    }

    public NettgameClientBootstrapBuilder buildChannelHandler(ChannelHandler channelHandler) {

        result.addChannelHandler(channelHandler);

        return this;
    }

    public NettgameClientBootstrapBuilder buildLogLevel(LogLevel logLevel) {

        result.setLogLevel(logLevel);

        return this;
    }

    public NettgameClientBootstrapBuilder buildReconnectAttempts(int i) {

        result.setReconnectAttempts(i);

        return this;
    }

    public NettgameClientBootstrapBuilder buildReconnectDelay(int i) {

        result.setReconnectDelay(i);

        return this;
    }

    public NettgameClientBootstrapBuilder buildShutdownTimeout(int i) {

        result.setShutdownTimeout(i);

        return this;
    }
}
