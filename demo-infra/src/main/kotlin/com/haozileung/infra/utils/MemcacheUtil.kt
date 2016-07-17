package com.haozileung.infra.utils

import com.whalin.MemCached.MemCachedClient
import com.whalin.MemCached.SockIOPool
import org.apache.commons.configuration2.builder.fluent.Configurations
import org.apache.commons.configuration2.ex.ConfigurationException
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object MemcacheUtil {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val cacheManager = MemCachedClient();
    private val pool = SockIOPool.getInstance()

    fun init() {
        try {
            val configs = Configurations();
            val c = configs.properties("memcache.properties")
            val serverStr = c.getString("servers", "");
            val servers = ArrayList<String>();
            for (s in serverStr.split(",")) {
                val st = s.trim()
                if (StringUtils.isNotEmpty(st)) {
                    servers.add(st);
                }
            }
            if (servers.size < 1) {
                throw RuntimeException("Memcache Config Server Size is < 1");
            }
            val pool = SockIOPool.getInstance();
            pool.servers = servers.toTypedArray()
            pool.failover = c.getBoolean("failover", true)
            pool.initConn = c.getInt("initConn", 100);
            pool.minConn = c.getInt("minConn", 25)
            pool.maxConn = c.getInt("maxConn", 250)
            pool.maintSleep = c.getLong("maintSleep", 30)
            pool.nagle = c.getBoolean("nagle", false)
            pool.socketTO = c.getInt("socketTO", 3000)
            pool.aliveCheck = c.getBoolean("aliveCheck", true)
            pool.hashingAlg = c.getInt("hashingAlg", 0)
            pool.socketConnectTO = c.getInt("socketConnectTO", 3000);
            val ws = c.getString("weights", "");
            val weights = ArrayList<Int>();
            for (s in ws.split(",")) {
                val st = s.trim()
                if (StringUtils.isNotEmpty(st)) {
                    weights.add(st.toInt());
                }
            }
            if (weights.size == servers.size) {
                pool.weights = weights.toTypedArray();
            }
            logger.info("Memcache SockIOPool initialize....");
            pool.initialize();
        } catch (e: ConfigurationException) {
            logger.error("Config File Load Error: {}", e.message)
        }
    }

    fun shutdown() {
        logger.info("Closing MemcacheUtil...")
        pool.shutDown()
    }

    fun <T> get(cacheName: String, key: Any, func: ICacheMissCallback<T>): T? {
        val element: T? = get(cacheName, key)
        if (element == null) {
            try {
                val value = func.callback()
                if (value != null) {
                    put(cacheName, key, value)
                }
                return value
            } catch (e: Exception) {
                logger.error(e.message, e)
                return null
            }

        } else {
            return element
        }
    }

    fun <T> get(cacheName: String, key: Any, time: Date, func: ICacheMissCallback<T>): T? {
        val element: T? = get(cacheName, key)
        if (element == null) {
            try {
                val value = func.callback()
                if (value != null) {
                    put(cacheName, key, value, time)
                }
                return value
            } catch (e: Exception) {
                logger.error(e.message, e)
                return null
            }

        } else {
            return element
        }
    }

    fun put(cacheName: String, key: Any, value: Any) {
        cacheManager.replace(cacheName + key, value)
    }

    fun put(cacheName: String, key: Any, value: Any, time: Date) {
        cacheManager.replace(cacheName + key, value, time)
    }

    operator fun <T> get(cacheName: String, key: Any): T? {
        val element = cacheManager.get(cacheName + key)
        return if (element != null) element as T else null
    }

    fun remove(cacheName: String, key: Any) {
        cacheManager.delete(cacheName + key)
    }

    fun removeAll() {
        cacheManager.flushAll()
    }
}