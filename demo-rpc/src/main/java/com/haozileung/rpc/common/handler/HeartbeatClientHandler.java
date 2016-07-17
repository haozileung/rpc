package com.haozileung.rpc.common.handler;

import com.haozileung.rpc.common.protocal.Heartbeat;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartbeatClientHandler extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.writeAndFlush(Heartbeat.BYTES).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        logger.debug("Finish to send a HEARTBEAT signal to the remote server while the state of channel is idle.");
                    } else {

                        logger.warn("Fail to send a HEARTBEAT signal to the remote server. channel proxy: {}", future.cause().getMessage());
                    }
                }
            });
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}