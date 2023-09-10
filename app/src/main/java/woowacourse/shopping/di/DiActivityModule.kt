package woowacourse.shopping.di

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.re4rk.arkdi.DiContainer
import com.re4rk.arkdi.HasDiContainer
import com.re4rk.arkdi.InstanceHolder
import com.re4rk.arkdi.Qualifier
import woowacourse.shopping.data.CartInDiskRepository
import woowacourse.shopping.data.CartProductDao
import woowacourse.shopping.data.ShoppingDatabase
import woowacourse.shopping.repository.CartRepository
import woowacourse.shopping.ui.cart.DateFormatter

class DiActivityModule(
    parentDiContainer: DiContainer?,
    private val context: Context,
) : DiContainer(parentDiContainer) {
    @Qualifier("ActivityContext")
    fun provideContext(): Context = Cache.context.get {
        context
    }

    @Qualifier("CartInDiskRepository")
    fun provideCartInDiskRepository(
        cartProductDao: CartProductDao,
    ): CartRepository = Cache.cartInDiskRepository.get {
        CartInDiskRepository(cartProductDao)
    }

    fun provideCartProductDao(
        shoppingDatabase: ShoppingDatabase,
    ): CartProductDao = Cache.cartProductDao.get {
        shoppingDatabase.cartProductDao()
    }

    fun provideDateFormatter(
        @Qualifier("ActivityContext") context: Context,
    ): DateFormatter = Cache.dataFormatter.get {
        DateFormatter(context)
    }

    private object Cache {
        val cartInDiskRepository = InstanceHolder<CartRepository>()
        val cartProductDao = InstanceHolder<CartProductDao>()
        val dataFormatter = InstanceHolder<DateFormatter>()
        val context = InstanceHolder<Context>()
    }

    companion object {
        fun create(activity: AppCompatActivity): DiActivityModule {
            return DiActivityModule(
                (activity.application as? HasDiContainer)?.diContainer,
                activity,
            )
        }
    }
}
