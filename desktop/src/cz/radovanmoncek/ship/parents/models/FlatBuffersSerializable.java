package cz.radovanmoncek.ship.parents.models;

import com.google.flatbuffers.FlatBufferBuilder;

/**
 * @author Radovan Monček
 */
public interface FlatBuffersSerializable {

    byte [] serialize(FlatBufferBuilder builder);
}
