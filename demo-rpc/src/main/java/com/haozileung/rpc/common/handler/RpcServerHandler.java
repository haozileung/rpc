package com.haozileung.rpc.common.handler;

import com.haozileung.rpc.common.exception.RpcException;
import com.haozileung.rpc.common.protocal.RpcRequest;
import com.haozileung.rpc.common.protocal.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.concurrent.ConcurrentMap;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private final ConcurrentMap<String, Object> serviceMapping;
    private Logger logger = LoggerFactory.getLogger(getClass());


    public RpcServerHandler(ConcurrentMap<String, Object> serviceMapping) {
        if (serviceMapping == null) {
            throw new RpcException("serviceMapping can't be null");
        }
        this.serviceMapping = serviceMapping;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcResponse response = new RpcResponse();
        response.setTraceId(request.getTraceId());
        try {
            String className = request.getClassName();
            Object serviceBean = serviceMapping.get(className);
            if (serviceBean != null) {
                Class serviceClass = serviceBean.getClass();
                String methodName = request.getMethodName();
                Class[] parameterTypes = request.getParameterTypes();
                Object[] parameters = request.getParameters();
                FastClass serviceFastClass = FastClass.create(serviceClass);
                FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
                if (serviceFastMethod != null) {
                    Object result = serviceFastMethod.invoke(serviceBean, parameters);
                    response.setResult(result);
                } else {
                    throw new RpcException("Method {} Not Exist!", methodName);
                }
            } else {
                throw new RpcException("Class {} Not Exist!", className);
            }
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
            response.setStatus(-1);
            response.setMsg(t.getMessage());
        }
        ctx.writeAndFlush(response);
    }
}
