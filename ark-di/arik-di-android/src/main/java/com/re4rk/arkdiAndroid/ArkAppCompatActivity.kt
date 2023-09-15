package com.re4rk.arkdiAndroid

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.reflect.KClass

open class ArkAppCompatActivity : AppCompatActivity() {
    private val arkViewModel: ArkViewModel
        by lazy { (application as ArkApplication).getActivityModule(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arkViewModel.ownerModule.inject(this)
    }

    fun <T : Any> injectViewModel(clazz: KClass<T>) =
        arkViewModel.viewModelModule.createInstance(clazz)
}
