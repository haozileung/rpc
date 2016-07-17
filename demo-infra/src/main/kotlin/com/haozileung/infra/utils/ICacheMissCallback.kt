package com.haozileung.infra.utils

interface ICacheMissCallback<T> {
    fun callback(): T?
}