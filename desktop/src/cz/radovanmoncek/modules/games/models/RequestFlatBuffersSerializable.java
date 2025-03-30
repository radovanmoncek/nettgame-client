package cz.radovanmoncek.modules.games.models;

import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.models.FlatBuffersSerializable;
import cz.radovanmoncek.ship.tables.Character;
import cz.radovanmoncek.ship.tables.Player;
import cz.radovanmoncek.ship.tables.Position;
import cz.radovanmoncek.ship.tables.Request;

import java.util.Objects;
import java.util.logging.Logger;

public class RequestFlatBuffersSerializable implements FlatBuffersSerializable {
    private static final Logger logger = Logger.getLogger(RequestFlatBuffersSerializable.class.getName());

    private byte gameStatus;
    private int[] position;
    private String name = "";
    private String gameCode = "";
    private byte character;

    public RequestFlatBuffersSerializable withGameStatus(byte gameStatus) {
        this.gameStatus = gameStatus;
        return this;
    }

    public RequestFlatBuffersSerializable withPosition(int[] position) {
        this.position = position;
        return this;
    }

    public RequestFlatBuffersSerializable withName(String name) {
        this.name = name;
        return this;
    }

    public RequestFlatBuffersSerializable withGameCode(String gameCode) {
        this.gameCode = gameCode;
        return this;
    }

    @Override
    public byte[] serialize(final FlatBufferBuilder builder) {

        logger.fine("Serializing RequestFlatBuffersSerializable");

        final var name = builder.createString(this.name);
        final var gameCode = builder.createString(this.gameCode);

        Player.startPlayer(builder);
        Player.addCharacter(builder, character);

        if(Objects.nonNull(position)) {
            Player.addPosition(builder, Position.createPosition(builder, this.position[0], this.position[1], this.position[2]));
        }

        Player.addName(builder, name);

        final var player = Player.endPlayer(builder);

        Request.startRequest(builder);
        Request.addGameStatus(builder, gameStatus);
        Request.addPlayer(builder, player);
        Request.addGameCode(builder, gameCode);

        final var gameStateRequest = Request.endRequest(builder);

        builder.finish(gameStateRequest);

        return builder.sizedByteArray();
    }

    public RequestFlatBuffersSerializable withCharacter(byte character) {

        this.character = character;

        return this;
    }
}
