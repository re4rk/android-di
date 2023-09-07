package woowacourse.shopping.di

import android.app.Application
import com.re4rk.arkdi.DiContainer

open class DiApplication : Application() {
    var diContainerLegacy: DiContainer = DiApplicationModule()
}
