package com.haozileung.rpc.server;

import com.haozileung.rpc.common.annotation.RpcService;
import com.haozileung.rpc.common.handler.HeartbeatServerHandler;
import com.haozileung.rpc.common.handler.RpcDecoder;
import com.haozileung.rpc.common.handler.RpcEncoder;
import com.haozileung.rpc.common.handler.RpcServerHandler;
import com.haozileung.rpc.common.protocal.RpcRequest;
import com.haozileung.rpc.common.protocal.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NettyServer implements ApplicationContextAware {
    private String host = null;
    private Integer ioThreadNum = Runtime.getRuntime().availableProcessors();
    private Integer backlog = 1024;
    private Integer port = 9000;
    private ChannelFuture channel = null;
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    private ConcurrentHashMap<String, Object> exportServiceMap = new ConcurrentHashMap<>();
    private DefaultEventExecutorGroup business = new DefaultEventExecutorGroup(Runtime.getRuntime().availableProcessors() * 2);
    private Logger logger = LoggerFactory.getLogger(getClass());

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getIoThreadNum() {
        return ioThreadNum;
    }

    public void setIoThreadNum(Integer ioThreadNum) {
        this.ioThreadNum = ioThreadNum;
    }

    public Integer getBacklog() {
        return backlog;
    }

    public void setBacklog(Integer backlog) {
        this.backlog = backlog;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 启动
     */
    @PostConstruct
    public void start() {
        logger.info("Begin to start rpc server");
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(ioThreadNum);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, backlog)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
                                .addLast("decoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4))
                                .addLast("encoder", new LengthFieldPrepender(4, false))
                                .addLast(new HeartbeatServerHandler())
                                .addLast(new RpcDecoder<>(RpcRequest.class))
                                .addLast(new RpcEncoder<>(RpcResponse.class))
                                .addLast(business, new RpcServerHandler(exportServiceMap));
                    }
                });

        channel = serverBootstrap.bind(host, port);
        logger.info("RPC server listening on port {} and ready for connections...", port);
    }

    @PreDestroy
    public void stop() {
        logger.info("destroy server resources");
        if (null == channel) {
            logger.error("server channel is null");
        }

        business.shutdownGracefully().syncUninterruptibly();
        channel.channel().close().syncUninterruptibly();
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        bossGroup = null;
        workerGroup = null;
        channel = null;
    }

    /**
     * 利用此方法获取spring ioc接管的所有bean
     */

    public void setApplicationContext(ApplicationContext ctx) {
        Map<String, Object> serviceMap = ctx.getBeansWithAnnotation(RpcService.class);
        logger.info("Fond RPC Service Bean:{}", serviceMap);
        if (serviceMap != null && serviceMap.size() > 0) {
            for (Object serviceBean : serviceMap.values()) {
                String interfaceName = "";
                Class<?>[] interfaces = serviceBean.getClass().getInterfaces();
                for (Class<?> clz : interfaces) {
                    if (clz.isAnnotationPresent(RpcService.class)) {
                        interfaceName = clz.getName();
                        break;
                    }
                }
                logger.info("Registering service :{}", interfaceName);
                exportServiceMap.put(interfaceName, serviceBean);
            }
        }
    }

}
