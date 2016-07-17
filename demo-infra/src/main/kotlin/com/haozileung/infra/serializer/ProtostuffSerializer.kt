/*
 * HaoziLeung's Demo
 */

package com.haozileung.infra.serializer

import com.dyuproject.protostuff.LinkedBuffer
import com.dyuproject.protostuff.ProtostuffIOUtil
import com.dyuproject.protostuff.Schema
import com.dyuproject.protostuff.runtime.RuntimeSchema
import org.objenesis.ObjenesisStd
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

/**
 * 序列化工具类（基于 Protostuff 实现）
 */
class ProtostuffSerializer<T : Serializable> : Serializer<T> {
    private val cachedSchema = ConcurrentHashMap<Class<T>, Schema<T>>()

    private fun getSchema(cls: Class<T>): Schema<T> {
        var schema: Schema<T>? = cachedSchema[cls]
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls)
            cachedSchema.put(cls, schema)
        }
        return schema as Schema<T>
    }

    override fun init() {

    }

    override fun serialize(obj: T): ByteArray {
        val buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE)
        try {
            val schema = getSchema(obj.javaClass)
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer)
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        } finally {
            buffer.clear()
        }
    }

    override fun deserialize(bytes: ByteArray, cls: Class<T>): T {
        try {
            val message = objenesis.newInstance(cls)
            val schema = getSchema(cls)
            ProtostuffIOUtil.mergeFrom(bytes, message, schema)
            return message
        } catch (e: Exception) {
            throw IllegalStateException(e.message, e)
        }

    }

    override fun register(clazz: Class<T>) {
        cachedSchema.put(clazz, RuntimeSchema.createFrom(clazz))
    }

    companion object {
        private val objenesis = ObjenesisStd(true)
    }
}
