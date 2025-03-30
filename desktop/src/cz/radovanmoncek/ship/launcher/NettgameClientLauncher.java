package cz.radovanmoncek.ship.launcher;

import cz.radovanmoncek.modules.games.handlers.GameServerChannelHandler;
import cz.radovanmoncek.ship.builders.NettgameClientBootstrapBuilder;
import cz.radovanmoncek.ship.directors.NettgameClientBootstrapDirector;

public final class NettgameClientLauncher {

    public static void main(String[] args) {

        new NettgameClientBootstrapDirector(new NettgameClientBootstrapBuilder())
                .makeDefaultGameClientBootstrapBuilder()
                .buildChannelHandler(new GameServerChannelHandler(true)) //todo: arg
                .buildShutdownTimeout(0)
                .build()
                .run();
    }
}
