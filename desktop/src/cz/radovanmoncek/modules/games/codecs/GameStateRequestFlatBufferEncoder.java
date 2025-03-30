package cz.radovanmoncek.modules.games.codecs;

import cz.radovanmoncek.modules.games.models.RequestFlatBuffersSerializable;
import com.google.flatbuffers.FlatBufferBuilder;
import cz.radovanmoncek.ship.parents.codecs.FlatBuffersEncoder;
import cz.radovanmoncek.ship.parents.models.FlatBuffersSerializable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameStateRequestFlatBufferEncoder extends FlatBuffersEncoder<RequestFlatBuffersSerializable> {
    private static final Logger logger = Logger.getLogger(GameStateRequestFlatBufferEncoder.class.getName());

    @Override
    protected byte[] encodeBodyAfterHeader(FlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

        logger.log(Level.INFO, "Encoding {0}", flatBuffersSerializable);

        return flatBuffersSerializable.serialize(flatBufferBuilder);
    }

    @Override
    protected byte[] encodeHeader(FlatBuffersSerializable flatBuffersSerializable, FlatBufferBuilder flatBufferBuilder) {

        return new byte[]{'g'};
    }
}
