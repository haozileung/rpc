package com.haozileung.infra.utils

import org.slf4j.LoggerFactory

/**
 * Created by Haozi on 2016/5/23.
 */
object CacheUtil {
    val logger = LoggerFactory.getLogger(javaClass)
    fun <T> get(cacheName: String, key: String, callback: ICacheMissCallback<T>): T? {
        return EhcacheUtil.get(cacheName, key, object : ICacheMissCallback<T> {
            override fun callback(): T? {
                val obj: T? = MemcacheUtil.get(cacheName, key)
                if (obj == null) {
                    val value: T? = callback.callback();
                    if (value != null) {
                        MemcacheUtil.put(cacheName, key, value)
                    }
                    return value
                } else {
                    ThreadUtil.addTask(Runnable {
                        val value: T? = callback.callback();
                        if (value != null) {
                            MemcacheUtil.put(cacheName, key, value)
                        }
                    })
                    return obj
                }
            }
        });
    }

    fun add(cacheName: String, key: Any, value: Any) {
        EhcacheUtil.remove(cacheName, key);
        MemcacheUtil.put(cacheName, key, value);
    }
}