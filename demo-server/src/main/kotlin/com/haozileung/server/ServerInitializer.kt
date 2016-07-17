package com.haozileung.server

import com.haozileung.server.config.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.AnnotationConfigApplicationContext


fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("Main")
    val context = AnnotationConfigApplicationContext(AppConfig::class.java)
    logger.info("Started...")
    context.registerShutdownHook()
}
