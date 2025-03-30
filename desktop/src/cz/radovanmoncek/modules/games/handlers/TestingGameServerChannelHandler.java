package cz.radovanmoncek.modules.games.handlers;

import com.badlogic.gdx.Gdx;
import cz.radovanmoncek.ship.tables.GameState;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Inspired by {@link io.netty.channel.embedded.EmbeddedChannel}
 */
public class TestingGameServerChannelHandler extends GameServerChannelHandler {
    private final LinkedBlockingQueue<GameState> queue = new LinkedBlockingQueue<>();
    private boolean initialized = false;

    public TestingGameServerChannelHandler(boolean windowed) {

        super(windowed);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GameState msg) {

        queue.offer(msg);

        super.channelRead0(ctx, msg);
    }

    public GameState poll() throws InterruptedException {

        return queue.poll(10000, TimeUnit.MILLISECONDS);
    }

    @Override
    public void disconnect() {

        Gdx
                .app
                .exit();

        queue.clear();
    }

    @Override
    public void unicast(final Object message) {

        super.unicast(message);
    }

    @Override
    public void create() {

        super.create();

        initialized = true;
    }

    public void awaitInitialization() throws InterruptedException {

        while (!initialized) {

            TimeUnit.MILLISECONDS.sleep(1000);
        }
    }
}
