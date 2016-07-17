package com.haozileung.server.service.impl

import com.haozileung.api.service.IEchoService
import org.springframework.stereotype.Service

/**
 * Created by haozileung on 2016/4/20.
 */
@Service
class EchoServiceImpl : IEchoService {
    override fun echo(s: String): String {
        return s
    }
}