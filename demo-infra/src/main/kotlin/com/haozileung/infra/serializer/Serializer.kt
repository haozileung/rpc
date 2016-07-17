/*
 * HaoziLeung's Demo
 */

package com.haozileung.infra.serializer

import java.io.Serializable

interface Serializer<T : Serializable> {

    fun init()

    fun serialize(obj: T): ByteArray

    fun deserialize(bytes: ByteArray, cls: Class<T>): T

    fun register(clazz: Class<T>)
}
