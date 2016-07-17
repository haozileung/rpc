package com.haozileung.server.config

import com.haozileung.infra.utils.PropUtils
import com.haozileung.rpc.server.NettyServer
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.dbutils.QueryRunner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.*
import org.springframework.core.env.Environment

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = arrayOf("com.haozileung.server"))
@PropertySource("classpath:/app.properties")
open class AppConfig {

    @Autowired
    lateinit var env: Environment

    @Bean(destroyMethod = "close")
    open fun dataSource(): HikariDataSource {
        val p = PropUtils.get("datasource.properties")
        val config = HikariConfig(p)
        val ds = HikariDataSource(config)
        logger.info("DataSource {} init...", ds.poolName)
        return ds
    }

    @Bean(name = arrayOf("dbutils"))
    open fun dbUtils(): QueryRunner {
        return QueryRunner(dataSource())
    }

    @Bean
    open fun rpcServer(): NettyServer {
        val server = NettyServer()
        server.backlog = 1024
        server.host = env.getProperty("rpc.host", "127.0.0.1")
        server.port = env.getProperty("rpc.port", Int::class.java, 9000)
        server.ioThreadNum = env.getProperty("rpc.ioThreadNum", Int::class.java, 4)
        return server
    }

    private val logger = LoggerFactory.getLogger(javaClass)

}
