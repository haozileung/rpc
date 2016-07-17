/*
 * HaoziLeung's Demo
 */

package com.haozileung.www.controller

import com.haozileung.infra.web.BaseServlet
import com.haozileung.api.service.IEchoService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by haozi on 16-4-9.
 */
@WebServlet(value = "/index", name = "index", loadOnStartup = 1)
class IndexController : BaseServlet() {

    @Autowired
    lateinit var echoService: IEchoService

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        logger.info(echoService.echo(RandomStringUtils.random(10, true, true)))
        render(req, resp)
    }
}