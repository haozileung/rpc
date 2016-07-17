package com.haozileung.rpc.spring;


import com.haozileung.rpc.common.exception.RpcException;
import com.haozileung.rpc.common.protocal.RpcRequest;
import com.haozileung.rpc.client.IClient;
import com.haozileung.rpc.client.netty.NettyClientFactory;
import com.haozileung.rpc.common.annotation.RpcSync;
import com.haozileung.rpc.common.protocal.ResponseFuture;
import com.haozileung.rpc.common.protocal.RpcResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cglib.proxy.Proxy;

public class SpringProxyFactoryBean implements InitializingBean, FactoryBean {

    private NettyClientFactory clientFactory;
    private String invokerInterface;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public void setClientFactory(NettyClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    public void setInvokerInterface(String invokerInterface) {
        this.invokerInterface = invokerInterface;
    }

    public Object getObject() throws Exception {
        final Class innerClass = Class.forName(invokerInterface);
        return Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{innerClass}, (o, method, objects) -> {
                    RpcRequest request = new RpcRequest();
                    request.setClassName(invokerInterface);
                    request.setMethodName(method.getName());
                    request.setParameterTypes(method.getParameterTypes());
                    request.setParameters(objects);
                    try {
                        RpcResponse response;
                        IClient client = clientFactory.get(innerClass);
                        if (client == null) {
                            logger.error("client can not be null");
                            return null;
                        }
                        if (method.isAnnotationPresent(RpcSync.class)) {
                            RpcSync sync = method.getAnnotation(RpcSync.class);
                            response = client.syncSend(request, sync.timeout(), sync.timeUnit());
                        } else if (method.getReturnType().equals(ResponseFuture.class)) {
                            return client.asyncSend(request);
                        } else {
                            response = client.syncSend(request);
                        }
                        if (response == null) {
                            logger.error("Rpc Response is NULL");
                            return null;
                        } else if (response.isError()) {
                            logger.error("Rpc Response With Error code:{} message:{}", response.getStatus(), response.getMsg());
                            return null;
                        } else {
                            return response.getResult();
                        }
                    } catch (RpcException e) {
                        logger.error(e.getMessage());
                        return null;
                    }
                });
    }

    public Class<?> getObjectType() {
        if (StringUtils.isNotBlank(invokerInterface)) {
            try {
                return Class.forName(invokerInterface);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage());
            }
        }
        return null;
    }

    public boolean isSingleton() {
        return true;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void close() {
        clientFactory.close();
    }
}