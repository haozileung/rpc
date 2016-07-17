/*
 * HaoziLeung's Demo
 */

package com.haozileung.infra.serializer

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.Input
import com.esotericsoftware.kryo.io.Output
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

class KryoSerializer<T : Serializable> : Serializer<T> {

    private val kryo: Kryo

    private val registeredClass: List<Class<T>>? = null

    init {
        this.kryo = Kryo()
        kryo.register(BigDecimal::class.java, DefaultSerializers.BigDecimalSerializer())
        kryo.register(BigInteger::class.java, DefaultSerializers.BigIntegerSerializer())
        kryo.register(Date::class.java, DefaultSerializers.DateSerializer())
    }

    @Throws(ClassNotFoundException::class)
    override fun init() {
        registeredClass?.forEach { this.register(it) }
    }

    @Throws(Exception::class)
    override fun serialize(obj: T): ByteArray {
        val bos = ByteArrayOutputStream()
        val output = Output(bos)
        kryo.writeObjectOrNull(output, obj, obj.javaClass)
        output.flush()
        output.clear()
        return bos.toByteArray()
    }

    @Throws(Exception::class)
    override fun deserialize(bytes: ByteArray, cls: Class<T>): T {
        val bais = ByteArrayInputStream(bytes)
        val input = Input(bais)
        return kryo.readObject(input, cls)
    }

    override fun register(clazz: Class<T>) {
        kryo.register(clazz)
    }
}
