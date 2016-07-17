package com.haozileung.www.init

import com.haozileung.infra.utils.ApplicationUtil
import org.slf4j.LoggerFactory
import org.springframework.web.WebApplicationInitializer
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import javax.servlet.ServletContext

class WebAppInitializer : WebApplicationInitializer {
    override fun onStartup(servletContext: ServletContext?) {
        val rootContext = AnnotationConfigWebApplicationContext()
        rootContext.register(AppConfig::class.java)
        rootContext.addBeanFactoryPostProcessor(RpcClientConfig())
        servletContext?.addListener(ContextLoaderListener(rootContext))
        ApplicationUtil.instance = rootContext
        logger.info("正在启动...")
    }

    private val logger = LoggerFactory.getLogger(this.javaClass)

}
