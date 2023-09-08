package woowacourse.shopping.di

import com.re4rk.arkdi.DiContainer
import com.re4rk.arkdi.Provides
import woowacourse.shopping.data.CartSampleRepository
import woowacourse.shopping.data.ProductSampleRepository
import woowacourse.shopping.repository.CartRepository
import woowacourse.shopping.repository.ProductRepository

object DiApplicationModule : DiContainer() {
    @Provides
    fun provideProductRepository(): ProductRepository = ProductSampleRepository()

    @Provides
    fun provideCartRepository(): CartRepository = CartSampleRepository()
}
