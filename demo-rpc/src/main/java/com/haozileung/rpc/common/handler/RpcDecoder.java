package com.haozileung.rpc.common.handler;

import com.haozileung.infra.serializer.KryoSerializer;
import com.haozileung.infra.serializer.Serializer;
import com.haozileung.rpc.common.compression.strategy.ThresholdCompressionStrategy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;

public class RpcDecoder<T extends Serializable> extends ByteToMessageDecoder {

    private final Class<T> genericClass;
    private Serializer<T> serializer = new KryoSerializer<>();
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ThresholdCompressionStrategy compressionStrategy = new ThresholdCompressionStrategy();

    public RpcDecoder(Class<T> genericClass) {
        this.genericClass = genericClass;
        serializer.register(genericClass);
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();
        int isCompress = in.readByte();
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);
        if (isCompress == 1) {
            data = compressionStrategy.decompress(data);
        }
        T obj = serializer.deserialize(data, genericClass);
        out.add(obj);
    }
}
