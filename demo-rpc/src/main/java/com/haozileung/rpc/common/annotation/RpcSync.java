package com.haozileung.rpc.common.annotation;


import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcSync {
    long timeout() default 5000L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
}