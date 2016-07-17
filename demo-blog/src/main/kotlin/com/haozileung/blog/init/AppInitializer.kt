package com.haozileung.server.web.init

import com.google.inject.Guice
import com.google.inject.Stage
import com.haozileung.infra.utils.EhcacheUtil
import com.haozileung.infra.utils.MemcacheUtil
import com.haozileung.infra.utils.ThreadUtil
import com.haozileung.infra.web.Initializer
import org.beetl.ext.servlet.ServletGroupTemplate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class AppInitializer : ServletContextListener {
    val logger: Logger = LoggerFactory.getLogger(javaClass)
    override fun contextInitialized(sce: ServletContextEvent) {
        Initializer.injector = Guice.createInjector(Stage.PRODUCTION)
        MemcacheUtil.init()
        logger.info("已启动...")
    }

    override fun contextDestroyed(sce: ServletContextEvent) {
        EhcacheUtil.shutdown()
        ThreadUtil.shutdown()
        MemcacheUtil.shutdown()
        ServletGroupTemplate.instance().groupTemplate.close()
        logger.info("已停止...")
    }
}
