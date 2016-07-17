package com.haozileung.rpc.common.handler;

import com.haozileung.rpc.common.protocal.Heartbeat;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatServerHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf.readableBytes() < 4) {
            if (Heartbeat.BYTES.length == byteBuf.readableBytes()) {
                ctx.writeAndFlush(msg).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isDone()) {
                            logger.debug("Finish to return a HEARTBEAT signal to the remote server while the state of channel is idle.");
                        } else {
                            logger.warn("Fail to return a HEARTBEAT signal to the remote server. channel proxy: {}", future.cause().getMessage());
                        }
                    }
                }).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                return;
            }
        }
        super.channelRead(ctx, msg);
    }
}
