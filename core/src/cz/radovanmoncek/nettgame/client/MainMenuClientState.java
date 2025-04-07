package cz.radovanmoncek.nettgame.client;

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
import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.nettgame.tables.*;
import cz.radovanmoncek.nettgame.tables.Character;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;

public class MainMenuClientState implements ClientState {
    private final Consumer<ByteBuffer> unicast;
    private Stage stage;
    private Texture background;
    private TextField inputStart,
            inputJoin1,
            inputJoin2;

    public MainMenuClientState(Consumer<ByteBuffer> unicast) {

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

                                        unicast.accept(serializeJoinRequest(inputJoin1.getText(), inputJoin2.getText()));
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

                            unicast.accept(serializeStartRequest(inputStart.getText().trim()));
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

    public ByteBuffer serializeStartRequest(final String requestedName) {

        final var builder = new FlatBufferBuilder(1024);
        final var name = builder.createString(requestedName);

        Player.startPlayer(builder);
        Player.addCharacter(builder, Character.BLUE);

        Player.addName(builder, name);

        final var player = Player.endPlayer(builder);

        Request.startRequest(builder);
        Request.addGameStatus(builder, GameStatus.START_SESSION);
        Request.addPlayer(builder, player);

        final var gameStateRequest = Request.endRequest(builder);

        builder.finish(gameStateRequest);

        final var array = builder.sizedByteArray();

        return ByteBuffer.wrap(array);
    }

    public ByteBuffer serializeJoinRequest(final String requestedName, final String requestedGameCode) {

        final var builder = new FlatBufferBuilder(1024);
        final var name = builder.createString(requestedName);
        final var gameCode = builder.createString(requestedGameCode);

        Player.startPlayer(builder);
        Player.addCharacter(builder, Character.RED);

        Player.addName(builder, name);

        final var player = Player.endPlayer(builder);

        Request.startRequest(builder);
        Request.addGameStatus(builder, GameStatus.JOIN_SESSION);
        Request.addPlayer(builder, player);
        Request.addGameCode(builder, gameCode);

        final var gameStateRequest = Request.endRequest(builder);

        builder.finish(gameStateRequest);

        final var array = builder.sizedByteArray();

        return ByteBuffer.wrap(array);
    }
}
