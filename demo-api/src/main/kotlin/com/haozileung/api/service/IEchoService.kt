package com.haozileung.api.service

import com.haozileung.rpc.common.annotation.RpcService

/**
 * Created by haozileung on 2016/4/20.
 */
@RpcService
interface IEchoService {
    fun echo(s: String): String
}