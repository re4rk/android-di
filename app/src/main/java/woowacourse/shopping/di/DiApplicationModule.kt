package woowacourse.shopping.di

import com.re4rk.arkdi.DiContainer
import woowacourse.shopping.data.CartSampleRepository
import woowacourse.shopping.data.ProductSampleRepository
import woowacourse.shopping.repository.CartRepository
import woowacourse.shopping.repository.ProductRepository

class DiApplicationModule : DiContainer() {

    val provideProductRepository: ProductRepository by lazy {
        this.createInstance(ProductSampleRepository::class)
    }

    val provideCartRepository: CartRepository by lazy {
        this.createInstance(CartSampleRepository::class)
    }
}
