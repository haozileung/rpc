package com.haozileung.rpc.common.exception;

public class RpcException extends RuntimeException {

    private String code;
    private String msg;

    public RpcException() {
        super();
    }

    public RpcException(String message) {
        super(message);
        msg = message;
    }

    public RpcException(String code, String msg) {
        super();
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
