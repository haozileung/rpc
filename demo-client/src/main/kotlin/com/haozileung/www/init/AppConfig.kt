/*
 * HaoziLeung's Demo
 */

package com.haozileung.www.init

import com.haozileung.infra.utils.PropUtils
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.dbutils.QueryRunner
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = arrayOf("com.haozileung.server"))
open class AppConfig {

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


    private val logger = LoggerFactory.getLogger(javaClass)

}
