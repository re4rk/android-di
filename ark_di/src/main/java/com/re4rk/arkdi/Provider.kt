package com.re4rk.arkdi

interface Provider<T> {
    fun get(): T
}
