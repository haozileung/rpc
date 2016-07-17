package com.haozileung.rpc.common.handler;


import com.haozileung.infra.serializer.KryoSerializer;
import com.haozileung.infra.serializer.Serializer;
import com.haozileung.rpc.common.compression.strategy.CompressionResult;
import com.haozileung.rpc.common.compression.strategy.ThresholdCompressionStrategy;
import com.haozileung.rpc.common.exception.RpcException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.Serializable;

public class RpcEncoder<T extends Serializable> extends MessageToByteEncoder {

    private final Class<T> genericClass;
    private Serializer<T> serializer = new KryoSerializer<>();
    private ThresholdCompressionStrategy compressionStrategy = new ThresholdCompressionStrategy();

    public RpcEncoder(Class<T> genericClass) {
        this.genericClass = genericClass;
        serializer.register(genericClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            byte[] data = serializer.serialize((T) msg);
            CompressionResult compressionResult = compressionStrategy.compress(data);
            int b = compressionResult.isCompressed() ? 1 : 0;
            out.writeByte(b);
            byte[] buff = compressionResult.getBuffer();
            if (buff == null) {
                throw new RpcException("Compressed result is null!");
            }
            out.writeInt(buff.length);
            out.writeBytes(buff);
        }
    }
}
