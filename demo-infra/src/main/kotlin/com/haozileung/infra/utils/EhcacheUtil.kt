package com.haozileung.infra.utils

import net.sf.ehcache.Cache
import net.sf.ehcache.CacheManager
import net.sf.ehcache.Element
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object EhcacheUtil {
    val logger: Logger = LoggerFactory.getLogger(javaClass)

    private val cacheManager = CacheManager.getInstance()

    fun shutdown() {
        logger.info("Closing EhcacheUtil...")
        cacheManager.shutdown()
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

    private fun getOrAddCache(cacheName: String): Cache {
        var cache: Cache? = cacheManager.getCache(cacheName)
        if (cache == null) {
            synchronized (cacheManager) {
                cache = cacheManager.getCache(cacheName)
                if (cache == null) {
                    logger.warn("Could not find cache config [$cacheName], using default.")
                    cacheManager.addCacheIfAbsent(cacheName)
                    cache = cacheManager.getCache(cacheName)
                    logger.debug("Cache [$cacheName] started.")
                }
            }
        }
        return cache!!
    }

    fun put(cacheName: String, key: Any, value: Any) {
        getOrAddCache(cacheName).put(Element(key, value))
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(cacheName: String, key: Any): T? {
        val element = getOrAddCache(cacheName).get(key)
        return if (element != null) element.objectValue as T else null
    }

    fun getKeys(cacheName: String): List<Any?> {
        return getOrAddCache(cacheName).keys
    }

    fun remove(cacheName: String, key: Any) {
        getOrAddCache(cacheName).remove(key)
    }

    fun removeAll(cacheName: String) {
        getOrAddCache(cacheName).removeAll()
    }
}