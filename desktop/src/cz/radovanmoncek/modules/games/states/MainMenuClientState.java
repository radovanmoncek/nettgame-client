package cz.radovanmoncek.modules.games.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import cz.radovanmoncek.modules.games.models.RequestFlatBuffersSerializable;
import cz.radovanmoncek.ship.parents.states.ClientState;
import cz.radovanmoncek.ship.tables.GameState;
import cz.radovanmoncek.ship.tables.GameStatus;
import cz.radovanmoncek.ship.tables.Character;

import java.util.LinkedList;
import java.util.function.Consumer;

public class MainMenuClientState implements ClientState {
    private final Consumer<RequestFlatBuffersSerializable> unicast;
    private Stage stage;
    private Texture background;
    private TextField inputStart,
            inputJoin1,
            inputJoin2;

    public MainMenuClientState(Consumer<RequestFlatBuffersSerializable> unicast) {

        this.unicast = unicast;
    }

    @Override
    public void processGameState(GameState gameState) {

    }

    @Override
    public void pingUpdate(long ping) {

    }

    @Override
    public void noViewportRender(Viewport viewport, SpriteBatch batch, float deltaTime) {

    }

    @Override
    public void render(Viewport viewport, SpriteBatch batch, float deltaTime) {

        stage.draw();
    }

    @Override
    public void registered() {

        Gdx
                .input
                .setInputProcessor(stage);
    }

    @Override
    public void initialize(LinkedList<Disposable> disposables) {

        background = new Texture("badlogic.jpg");
        stage = new Stage();

        //https://stackoverflow.com/questions/21488311/how-to-create-a-button-in-libgdx
        final var font = new BitmapFont();
        final var style = new TextButton.TextButtonStyle();

        style.font = font;
        style.fontColor = Color.WHITE;

        final var buttonJoin = new TextButton("Join existing session", style);
        final var button = new TextButton("New Session", style);

        buttonJoin.setX(Gdx.graphics.getWidth() / (float) 2 - buttonJoin.getWidth() / 2);
        buttonJoin.setY(Gdx.graphics.getHeight() / (float) 2 - 50);
        buttonJoin.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                stage.clear();

                final var inputStyle = new TextField.TextFieldStyle();

                inputStyle.font = font;
                inputStyle.fontColor = Color.WHITE;

                inputJoin1 = new TextField("Please enter your nickname", inputStyle);

                inputJoin1.setX(Gdx.graphics.getWidth() / (float) 2 - inputJoin1.getWidth() / 2);
                inputJoin1.setY(Gdx.graphics.getHeight() / (float) 2);
                inputJoin1.setSize(200, 100);

                stage.addActor(inputJoin1);

                inputJoin1.addListener(new InputListener() {

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                        inputJoin1.setText("");

                        return true;
                    }

                    @Override
                    public boolean keyTyped(InputEvent event, char character) {

                        if (character == 10) {

                            stage.clear();

                            final var inputStyle = new TextField.TextFieldStyle();

                            inputStyle.font = font;
                            inputStyle.fontColor = Color.WHITE;

                            inputJoin2 = new TextField("Please enter game code", inputStyle);
                            inputJoin2.setX(Gdx.graphics.getWidth() / (float) 2 - inputJoin2.getWidth() / 2);
                            inputJoin2.setY(Gdx.graphics.getHeight() / (float) 2);
                            inputJoin2.setSize(200, 100);
                            inputJoin2.addListener(new InputListener() {

                                @Override
                                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                                    inputJoin2.setText("");

                                    return true;
                                }

                                @Override
                                public boolean keyTyped(InputEvent event, char character) {

                                    if (character == 10) {

                                        final var serializable = new RequestFlatBuffersSerializable()
                                                .withGameStatus(GameStatus.JOIN_SESSION)
                                                .withName(inputJoin1.getText())
                                                .withGameCode(inputJoin2.getText())
                                                .withCharacter(Character.BLUE);

                                        unicast.accept(serializable);
                                    }

                                    return true;
                                }
                            });

                            stage.addActor(inputJoin2);
                        }

                        return true;
                    }
                });
            }
        });

        button.setX(Gdx.graphics.getWidth() / (float) 2 - button.getWidth() / 2);
        button.setY(Gdx.graphics.getHeight() / (float) 2);
        button.addListener(new ChangeListener() {

            @Override
            public void changed(ChangeEvent event, Actor actor) {

                stage.clear();

                final var inputStyle = new TextField.TextFieldStyle();

                inputStyle.font = font;
                inputStyle.fontColor = Color.WHITE;

                inputStart = new TextField("Please enter your nickname", inputStyle);
                inputStart.setX(Gdx.graphics.getWidth() / (float) 2 - inputStart.getWidth() / 2);
                inputStart.setY(Gdx.graphics.getHeight() / (float) 2);
                inputStart.setSize(200, 100);

                stage.addActor(inputStart);

                inputStart.addListener(new InputListener() {

                    @Override
                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {

                        inputStart.setText("");

                        return true;
                    }

                    @Override
                    public boolean keyTyped(InputEvent event, char character) {

                        if (character == 10) {

                            final var serializable = new RequestFlatBuffersSerializable()
                                    .withGameStatus(GameStatus.START_SESSION)
                                    .withName(inputStart
                                            .getText()
                                            .trim()
                                    );

                            unicast.accept(serializable);
                        }

                        return true;
                    }
                });
            }
        });

        stage.addActor(button);
        stage.addActor(buttonJoin);

        disposables.add(stage);
        disposables.add(background);
    }
}
