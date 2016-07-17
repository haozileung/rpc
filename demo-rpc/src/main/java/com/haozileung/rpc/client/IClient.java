package com.haozileung.rpc.client;

import com.haozileung.rpc.common.protocal.RpcRequest;
import com.haozileung.rpc.common.protocal.RpcResponse;
import com.haozileung.rpc.common.protocal.ResponseFuture;

import java.util.concurrent.TimeUnit;

public interface IClient {

    void connect();

    RpcResponse syncSend(RpcRequest request);

    RpcResponse syncSend(RpcRequest request, Long time, TimeUnit unit);

    ResponseFuture<RpcResponse> asyncSend(RpcRequest request);

    void close();

    boolean isClose();
}
