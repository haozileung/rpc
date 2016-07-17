package com.haozileung.rpc.common.handler;

import com.haozileung.rpc.common.exception.RpcException;
import com.haozileung.rpc.common.protocal.ResponseFuture;
import com.haozileung.rpc.common.protocal.RpcResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

@ChannelHandler.Sharable
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private final ConcurrentMap<String, ResponseFuture<RpcResponse>> responseMap;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public RpcClientHandler(ConcurrentMap<String, ResponseFuture<RpcResponse>> responseMap) {
        if (responseMap == null) {
            throw new RpcException("responseMap can't be null");
        }
        this.responseMap = responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        String id = rpcResponse.getTraceId();
        ResponseFuture<RpcResponse> future = this.responseMap.get(id);
        if (future != null) {
            future.commit(rpcResponse);
        } else {
            throw new RpcException("Fail to find any matching future of response.");
        }
    }
}
