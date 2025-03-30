package cz.radovanmoncek.ship.parents.states;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import cz.radovanmoncek.ship.tables.GameState;

import java.util.LinkedList;

/**
 * State machine pattern for reacting and representing different states of the currently running game session.
 * @author Radovan Monček
 */
public interface ClientState {

    void initialize(LinkedList<Disposable> disposables);

    void noViewportRender(Viewport viewport, SpriteBatch batch, float deltaTime);

    void render(Viewport viewport, SpriteBatch batch, float deltaTime);

    void processGameState(GameState gameState);

    void pingUpdate(long ping);

    void registered();
}
