package com.re4rk.arkdi

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class Provides(val value: String = "")
