package com.haozileung.rpc.client.netty;


import com.haozileung.rpc.client.IClient;
import com.haozileung.rpc.common.exception.RpcException;
import com.haozileung.rpc.common.handler.HeartbeatClientHandler;
import com.haozileung.rpc.common.handler.RpcClientHandler;
import com.haozileung.rpc.common.handler.RpcDecoder;
import com.haozileung.rpc.common.handler.RpcEncoder;
import com.haozileung.rpc.common.protocal.ResponseFuture;
import com.haozileung.rpc.common.protocal.RpcRequest;
import com.haozileung.rpc.common.protocal.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NettyClient implements IClient {

    private final InetSocketAddress socketAddress;
    private Logger logger = LoggerFactory.getLogger(getClass());
    private ConcurrentHashMap<String, ResponseFuture<RpcResponse>> responseMap = new ConcurrentHashMap<>();
    private Integer workerGroupThreads = 8;
    private Long readTimeout = 0L;
    private Long writeTimeout = 0L;
    private Long allTimeout = 180L;
    private EventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private volatile Channel channel;
    private RpcClientHandler clientRpcHandler = new RpcClientHandler(responseMap);
    private volatile boolean closed = false;

    public NettyClient(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;

    }

    public Integer getWorkerGroupThreads() {
        return workerGroupThreads;
    }

    public void setWorkerGroupThreads(Integer workerGroupThreads) {
        this.workerGroupThreads = workerGroupThreads;
    }

    public Long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public Long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(Long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Long getAllTimeout() {
        return allTimeout;
    }

    public void setAllTimeout(Long allTimeout) {
        this.allTimeout = allTimeout;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void connect() {
        try {
            workerGroup = new NioEventLoopGroup(workerGroupThreads);
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                    super.channelInactive(ctx);
                                    ctx.channel().eventLoop().schedule(() -> doConnect(socketAddress), 1L, TimeUnit.SECONDS);
                                }
                            }).addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                    .addLast("encoder", new LengthFieldPrepender(4, false))
                                    .addLast(new IdleStateHandler(readTimeout, writeTimeout, allTimeout, TimeUnit.SECONDS))
                                    .addLast(new HeartbeatClientHandler())
                                    .addLast(new RpcDecoder<>(RpcResponse.class))
                                    .addLast(new RpcEncoder<>(RpcRequest.class))
                                    .addLast(clientRpcHandler);
                        }
                    });
            doConnect(socketAddress);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public RpcResponse syncSend(RpcRequest request) {
        ResponseFuture<RpcResponse> future = new ResponseFuture<>(request.getTraceId(), this.responseMap);
        this.responseMap.put(request.getTraceId(), future);
        channel.writeAndFlush(request);
        RpcResponse response = future.get();
        if (future.isCancelled()) {
            throw new RpcException(" response future is cancelled.");
        }
        return response;
    }

    @Override
    public RpcResponse syncSend(RpcRequest request, Long time, TimeUnit unit) {
        ResponseFuture<RpcResponse> future = new ResponseFuture<>(request.getTraceId(), this.responseMap);
        this.responseMap.put(request.getTraceId(), future);
        channel.writeAndFlush(request);
        RpcResponse response = future.get(time, unit);
        if (future.isCancelled()) {
            throw new RpcException(" response future is cancelled.");
        }
        return response;
    }

    @Override
    public ResponseFuture<RpcResponse> asyncSend(RpcRequest request) {
        ResponseFuture<RpcResponse> future = new ResponseFuture<>(request.getTraceId(), this.responseMap);
        this.responseMap.put(request.getTraceId(), future);
        channel.writeAndFlush(request);
        return future;
    }

    @Override
    public void close() {
        logger.info("destroy client resources");
        closed = true;
        if (null == channel) {
            logger.error("channel is null");
        }
        channel.close().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup = null;
        channel = null;
    }

    @Override
    public boolean isClose() {
        return closed;
    }

    private void doConnect(InetSocketAddress socketAddress) {
        if (closed) {
            return;
        }
        logger.info("trying to connect server:{}", socketAddress);
        ChannelFuture future = bootstrap.connect(socketAddress);
        future.addListener((ChannelFutureListener) future1 -> {
            if (!future1.isSuccess()) {
                logger.info("connected to {} failed", socketAddress);
                future1.channel().eventLoop().schedule(() -> doConnect(socketAddress), 1L, TimeUnit.SECONDS);
            } else {
                logger.info("connected to {}", socketAddress);
            }
        });
        channel = future.syncUninterruptibly().channel();
    }
}
