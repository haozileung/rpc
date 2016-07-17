package com.haozileung.infra.utils

import org.apache.commons.configuration2.ConfigurationConverter
import org.apache.commons.configuration2.FileBasedConfiguration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.ex.ConfigurationException
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Haozi
 * @version 1.0.0
 */
object PropUtils {
    private val logger = LoggerFactory.getLogger(EhcacheUtil::class.java)
    fun get(name: String): Properties? {
        val params = Parameters()
        val builder = FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration::class.java).configure(params.properties().setFileName(name))
        try {
            return ConfigurationConverter.getProperties(builder.configuration)
        } catch(cex: ConfigurationException) {
            logger.error(cex.message)
        }
        return null
    }
}
