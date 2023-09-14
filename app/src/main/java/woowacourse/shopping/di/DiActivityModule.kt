package woowacourse.shopping.di

import android.content.Context
import com.re4rk.arkdi.DiContainer
import com.re4rk.arkdi.Singleton
import com.re4rk.arkdi.annotations.ContextType
import com.re4rk.arkdi.annotations.ContextType.Type.ACTIVITY
import com.re4rk.arkdi.annotations.StorageType
import com.re4rk.arkdi.annotations.StorageType.Type.DATABASE
import woowacourse.shopping.data.CartInDiskRepository
import woowacourse.shopping.data.CartProductDao
import woowacourse.shopping.data.ShoppingDatabase
import woowacourse.shopping.repository.CartRepository
import woowacourse.shopping.ui.cart.DateFormatter

class DiActivityModule(
    parentDiContainer: DiContainer?,
    private val context: Context,
) : DiContainer(parentDiContainer) {
    @Singleton
    @ContextType(ACTIVITY)
    fun provideContext(): Context = context

    @Singleton
    @StorageType(DATABASE)
    fun provideCartInDiskRepository(
        cartProductDao: CartProductDao,
    ): CartRepository = CartInDiskRepository(cartProductDao)

    @Singleton
    fun provideCartProductDao(
        shoppingDatabase: ShoppingDatabase,
    ): CartProductDao = shoppingDatabase.cartProductDao()

    @Singleton
    fun provideDateFormatter(
        @ContextType(ACTIVITY) context: Context,
    ): DateFormatter = DateFormatter(context)
}
