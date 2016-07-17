/*
 * HaoziLeung's Demo
 */

package com.haozileung.infra.serializer

import java.io.*

class ObjectSerializer<T : Serializable> : Serializer<T> {

    @Throws(Exception::class)
    override fun init() {
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(bytes: ByteArray, cls: Class<T>): T {
        var ois: ObjectInputStream? = null
        try {
            ois = ObjectInputStream(ByteArrayInputStream(bytes))
            return ois.readObject() as T
        } finally {
            if (ois != null) {
                ois.close()
            }
        }
    }

    @Throws(Exception::class)
    override fun serialize(obj: T): ByteArray {
        var outputStream: ByteArrayOutputStream? = null
        var oos: ObjectOutputStream? = null
        try {
            outputStream = ByteArrayOutputStream()
            oos = ObjectOutputStream(outputStream)
            oos.writeObject(obj)
            return outputStream.toByteArray()
        } finally {
            if (oos != null) {
                oos.close()
            }
            if (outputStream != null) {
                outputStream.close()
            }
        }
    }

    override fun register(clazz: Class<T>) {
    }

}
