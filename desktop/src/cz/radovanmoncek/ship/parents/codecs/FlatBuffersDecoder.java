package cz.radovanmoncek.ship.parents.codecs;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.logging.Logger;

public abstract class FlatBuffersDecoder<FlatBuffersSchema> extends ByteToMessageDecoder {
    private static final Logger logger = Logger.getLogger(FlatBuffersDecoder.class.getName());
    private static final int lengthFieldSize = Long.BYTES;

    @Override
    protected void decode(final ChannelHandlerContext channelHandlerContext, final ByteBuf in, final List<Object> out) {

        if (in.readableBytes() < lengthFieldSize) {

            return;
        }

        in.markReaderIndex();

        final var length = in.readLong();
        final var headerLength = 1;
        final var bodyLength = (int) length - headerLength;

        if (in.readableBytes() < length) {

            in.resetReaderIndex();

            return;
        }

        if (in.readableBytes() == 0) {

            logger.warning("Received an empty flat buffer");

            return;
        }

        final var header = in.readBytes(headerLength).nioBuffer();

        if (!decodeHeader(header)) {

            channelHandlerContext.fireChannelRead(in.resetReaderIndex().retain());

            return;
        }

        final var body = in.readBytes(bodyLength).nioBuffer();
        final var decodedSchema = decodeBodyAfterHeader(body);

        out.add(decodedSchema);
    }

    protected abstract boolean decodeHeader(final ByteBuffer buffer);

    protected abstract FlatBuffersSchema decodeBodyAfterHeader(final ByteBuffer buffer);
}
